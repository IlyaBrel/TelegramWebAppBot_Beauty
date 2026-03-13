package ibrel.tgBeautyWebApp.service.booking.impl;

import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.booking.AppointmentServiceItem;
import ibrel.tgBeautyWebApp.model.booking.AppointmentSlot;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentServiceItemRepository;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentSlotRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterServiceWorkRepository;
import ibrel.tgBeautyWebApp.repository.master.VariableServiceDetailsRepository;
import ibrel.tgBeautyWebApp.repository.master.WorkSlotRepository;
import ibrel.tgBeautyWebApp.service.tg.TelegramNotificationService;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final AppointmentServiceItemRepository itemRepository;
    private final MasterRepository masterRepository;
    private final WorkSlotRepository workSlotRepository;
    private final MasterServiceWorkRepository masterServiceWorkRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;
    private final TelegramNotificationService telegramService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ── Создание заказа ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public Appointment createAppointment(Long clientTelegramId,
                                         Long masterId,
                                         Long startSlotId,
                                         List<ServiceSelectionDto> services,
                                         String promoCode,
                                         BigDecimal bonusAmountToUse) {

        Assert.notNull(clientTelegramId, "clientTelegramId must not be null");
        Assert.notNull(masterId,         "masterId must not be null");
        Assert.notNull(startSlotId,      "startSlotId must not be null");
        Assert.notEmpty(services,        "services must not be empty");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        WorkSlot startSlot = workSlotRepository.findById(startSlotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + startSlotId));

        if (!startSlot.getMaster().getId().equals(masterId)) {
            throw new IllegalArgumentException("Slot does not belong to master");
        }

        // ── Подсчёт услуг ────────────────────────────────────────────────────
        CalcResult calc = calculateServices(masterId, services);

        // ── Подбор нужного количества слотов ─────────────────────────────────
        List<WorkSlot> bookedSlots = resolveSlots(master, startSlot, calc.totalDuration);

        // Проверяем что все слоты свободны
        for (WorkSlot slot : bookedSlots) {
            if (appointmentSlotRepository.existsBySlotIdAndActive(slot.getId(),
                    Appointment.Status.CANCELLED,
                    Appointment.Status.REJECTED)) {
                throw new IllegalStateException("Slot id=" + slot.getId() + " is already booked");
            }
        }

        BigDecimal bonusUsed  = bonusAmountToUse != null ? bonusAmountToUse : BigDecimal.ZERO;

        // ── Сборка заказа ─────────────────────────────────────────────────────
        Appointment appointment = Appointment.builder()
                .clientTelegramId(clientTelegramId)
                .master(master)
                .status(Appointment.Status.PENDING)
                .totalDurationMinutes(calc.totalDuration)
                .totalPrice(calc.totalPrice)
                .bonusAmountUsed(bonusUsed)
                .createdAt(OffsetDateTime.now())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // Привязываем слоты
        for (int i = 0; i < bookedSlots.size(); i++) {
            AppointmentSlot as = AppointmentSlot.builder()
                    .appointment(saved)
                    .slot(bookedSlots.get(i))
                    .slotOrder(i)
                    .build();
            appointmentSlotRepository.save(as);
        }

        // Привязываем позиции услуг
        for (AppointmentServiceItem item : calc.items) {
            item.setAppointment(saved);
            itemRepository.save(item);
        }

        // Уведомляем мастера
        String slotDate = startSlot.getDayOfWeek();
        String slotTime = startSlot.getStartTime().format(TIME_FMT);
        String servicesStr = calc.items.stream()
                .map(i -> i.getService().getName())
                .collect(Collectors.joining(", "));

        String clientName = "Пользователь #" + clientTelegramId;
        try {
            telegramService.notifyMasterNewAppointment(
                    master.getTelegramId(), saved.getId(),
                    clientName, slotDate, slotTime, servicesStr);
        } catch (Exception e) {
            log.warn("Could not notify master telegramId={}: {}", master.getTelegramId(), e.getMessage());
        }

        // Уведомляем клиента
        String masterName = master.getPersonalData() != null
                ? master.getPersonalData().getFirstName() : "Мастер";
        try {
            telegramService.notifyAppointmentPending(
                    clientTelegramId, masterName, slotDate, slotTime);
        } catch (Exception e) {
            log.warn("Could not notify client telegramId={}: {}", clientTelegramId, e.getMessage());
        }

        log.info("Created appointment id={} masterId={} clientTelegramId={} slots={}",
                saved.getId(), masterId, clientTelegramId, bookedSlots.size());
        return saved;
    }

    // ── Получение ─────────────────────────────────────────────────────────────

    @Override
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found id=" + id));
    }

    @Override
    public List<Appointment> getByMaster(Long masterId) {
        if (!masterRepository.existsById(masterId))
            throw new EntityNotFoundException("Master not found id=" + masterId);
        return appointmentRepository.findByMaster_IdOrderByCreatedAtDesc(masterId);
    }

    @Override
    public List<Appointment> getByClient(Long clientTelegramId) {
        return appointmentRepository.findByClientTelegramIdOrderByCreatedAtDesc(clientTelegramId);
    }

//    @Override
//    public List<Appointment> getByClientAndStatus(Long clientTelegramId, Appointment.Status status) {
//        return appointmentRepository.findByClientTelegramIdAndStatusOrderByCreatedAtDesc(clientTelegramId, status);
//    }

    // ── Отмена пользователем ──────────────────────────────────────────────────

    @Override
    @Transactional
    public Appointment cancelByClient(Long appointmentId, Long clientTelegramId) {
        Appointment a = getById(appointmentId);

        if (!a.getClientTelegramId().equals(clientTelegramId)) {
            throw new IllegalArgumentException("You can only cancel your own appointments");
        }
        if (a.getStatus() == Appointment.Status.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }
        if (a.getStatus() == Appointment.Status.CANCELLED) {
            return a; // идемпотентно
        }

        a.setStatus(Appointment.Status.CANCELLED);
        a.setUpdatedAt(OffsetDateTime.now());
        Appointment saved = appointmentRepository.save(a);

        // Уведомляем мастера
        String masterName = a.getMaster().getPersonalData() != null
                ? a.getMaster().getPersonalData().getFirstName() : "Мастер";
        String clientName = "Пользователь #" + clientTelegramId;
        WorkSlot firstSlot = getFirstSlot(a);
        try {
            telegramService.notifyMasterAppointmentCancelled(
                    a.getMaster().getTelegramId(), clientName,
                    firstSlot != null ? firstSlot.getDayOfWeek() : "—",
                    firstSlot != null ? firstSlot.getStartTime().format(TIME_FMT) : "—");
        } catch (Exception e) {
            log.warn("Could not notify master about cancellation: {}", e.getMessage());
        }

        log.info("Cancelled appointment id={} by client={}", appointmentId, clientTelegramId);
        return saved;
    }

    // ── Подтверждение мастером ────────────────────────────────────────────────

    @Override
    @Transactional
    public Appointment confirmByMaster(Long appointmentId, Long masterTelegramId) {
        Appointment a = getById(appointmentId);
        requireMasterOwner(a, masterTelegramId);

        if (a.getStatus() != Appointment.Status.PENDING) {
            throw new IllegalStateException("Only PENDING appointment can be confirmed");
        }

        a.setStatus(Appointment.Status.CONFIRMED);
        a.setUpdatedAt(OffsetDateTime.now());
        Appointment saved = appointmentRepository.save(a);

        String masterName = a.getMaster().getPersonalData() != null
                ? a.getMaster().getPersonalData().getFirstName() : "Мастер";
        WorkSlot firstSlot = getFirstSlot(a);
        try {
            telegramService.notifyAppointmentConfirmed(
                    a.getClientTelegramId(), masterName,
                    firstSlot != null ? firstSlot.getDayOfWeek() : "—",
                    firstSlot != null ? firstSlot.getStartTime().format(TIME_FMT) : "—");
        } catch (Exception e) {
            log.warn("Could not notify client about confirmation: {}", e.getMessage());
        }

        log.info("Confirmed appointment id={} by master={}", appointmentId, masterTelegramId);
        return saved;
    }

    // ── Отклонение мастером ───────────────────────────────────────────────────

    @Override
    @Transactional
    public Appointment rejectByMaster(Long appointmentId, Long masterTelegramId, String reason) {
        Appointment a = getById(appointmentId);
        requireMasterOwner(a, masterTelegramId);

        if (a.getStatus() != Appointment.Status.PENDING) {
            throw new IllegalStateException("Only PENDING appointment can be rejected");
        }

        a.setStatus(Appointment.Status.REJECTED);
        a.setRejectionReason(reason);
        a.setUpdatedAt(OffsetDateTime.now());
        Appointment saved = appointmentRepository.save(a);

        String masterName = a.getMaster().getPersonalData() != null
                ? a.getMaster().getPersonalData().getFirstName() : "Мастер";
        try {
            telegramService.notifyAppointmentRejected(a.getClientTelegramId(), masterName, reason);
        } catch (Exception e) {
            log.warn("Could not notify client about rejection: {}", e.getMessage());
        }

        log.info("Rejected appointment id={} by master={} reason={}", appointmentId, masterTelegramId, reason);
        return saved;
    }

    // ── Завершение ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Appointment complete(Long appointmentId, Long masterTelegramId) {
        Appointment a = getById(appointmentId);
        requireMasterOwner(a, masterTelegramId);

        if (a.getStatus() != Appointment.Status.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED appointment can be completed");
        }

        a.setStatus(Appointment.Status.COMPLETED);
        a.setUpdatedAt(OffsetDateTime.now());
        Appointment saved = appointmentRepository.save(a);
        log.info("Completed appointment id={}", appointmentId);
        return saved;
    }

    // ── Запрос на отзыв ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void sendReviewRequest(Long appointmentId) {
        Appointment a = getById(appointmentId);
        if (Boolean.TRUE.equals(a.getReviewRequestSent())) return;

        String masterName = a.getMaster().getPersonalData() != null
                ? a.getMaster().getPersonalData().getFirstName() : "мастера";
        try {
            telegramService.notifyReviewRequest(a.getClientTelegramId(), appointmentId, masterName);
            a.setReviewRequestSent(true);
            appointmentRepository.save(a);
        } catch (Exception e) {
            log.warn("Could not send review request for appointment id={}: {}", appointmentId, e.getMessage());
        }
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    /**
     * Подбирает список слотов подряд начиная со startSlot,
     * чтобы покрыть totalDurationMinutes.
     */
    private List<WorkSlot> resolveSlots(Master master, WorkSlot startSlot, int totalDuration) {
        long startSlotMinutes = Duration.between(startSlot.getStartTime(), startSlot.getEndTime()).toMinutes();

        if (totalDuration <= startSlotMinutes) {
            return List.of(startSlot); // хватает одного слота
        }

        // Ищем свободные слоты с того же дня начиная со времени startSlot
        List<WorkSlot> candidates = workSlotRepository.findFreeFrom(
                master.getId(), startSlot.getDayOfWeek(), startSlot.getStartTime());

        List<WorkSlot> result   = new ArrayList<>();
        int            covered  = 0;
        LocalTime      expected = startSlot.getStartTime();

        for (WorkSlot slot : candidates) {
            if (!slot.getStartTime().equals(expected)) break; // слоты должны идти подряд

            result.add(slot);
            covered += (int) Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
            expected = slot.getEndTime();

            if (covered >= totalDuration) break;
        }

        if (covered < totalDuration) {
            throw new IllegalStateException(
                    "Not enough consecutive free slots for duration=" + totalDuration + " min");
        }

        return result;
    }

    /** Считает итоговую стоимость, длительность и формирует items. */
    private CalcResult calculateServices(Long masterId,
                                         List<ServiceSelectionDto> services) {
        int    totalDuration = 0;
        double totalPrice    = 0.0;
        List<AppointmentServiceItem> items = new ArrayList<>();

        for (ServiceSelectionDto sel : services) {
            MasterServiceWork service = masterServiceWorkRepository.findById(sel.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + sel.getServiceId()));

            if (!service.getMaster().getId().equals(masterId)) {
                throw new IllegalArgumentException("Service id=" + sel.getServiceId() + " does not belong to master");
            }
            if (!Boolean.TRUE.equals(service.getActive())) {
                throw new IllegalArgumentException("Service id=" + sel.getServiceId() + " is not active");
            }

            List<VariableServiceDetails> vars = new ArrayList<>();

            switch (service.getType()) {
                case FIXED -> {
                    if (service.getFixedDetails() == null)
                        throw new IllegalArgumentException("Fixed service has no details id=" + service.getId());
                    totalDuration += Optional.ofNullable(service.getFixedDetails().getDurationMinutes()).orElse(0);
                    totalPrice    += Optional.ofNullable(service.getFixedDetails().getPrice()).orElse(0.0);
                }
                case VARIABLE -> {
                    if (sel.getVariableDetailIds() == null || sel.getVariableDetailIds().isEmpty())
                        throw new IllegalArgumentException("Variable service requires detail ids id=" + service.getId());
                    for (Long varId : sel.getVariableDetailIds()) {
                        VariableServiceDetails v = variableServiceDetailsRepository.findById(varId)
                                .orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + varId));
                        if (!v.getService().getId().equals(service.getId()))
                            throw new IllegalArgumentException("Variable detail does not belong to service");
                        vars.add(v);
                        totalDuration += Optional.ofNullable(v.getDurationMinutes()).orElse(0);
                        totalPrice    += Optional.ofNullable(v.getPrice()).orElse(0.0);
                    }
                }
            }

            items.add(AppointmentServiceItem.builder()
                    .service(service)
                    .variableDetails(vars)
                    .build());
        }

        return new CalcResult(totalDuration, totalPrice, items);
    }

    private void requireMasterOwner(Appointment a, Long masterTelegramId) {
        if (!a.getMaster().getTelegramId().equals(masterTelegramId)) {
            throw new IllegalArgumentException("You are not the master of this appointment");
        }
    }

    private WorkSlot getFirstSlot(Appointment a) {
        if (a.getSlots() == null || a.getSlots().isEmpty()) return null;
        return a.getSlots().get(0).getSlot();
    }

    private record CalcResult(int totalDuration, double totalPrice, List<AppointmentServiceItem> items) {}
}