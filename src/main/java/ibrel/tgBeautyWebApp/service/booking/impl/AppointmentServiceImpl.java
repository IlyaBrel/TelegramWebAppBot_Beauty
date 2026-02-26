package ibrel.tgBeautyWebApp.service.booking.impl;


import ibrel.tgBeautyWebApp.dto.booking.AppointmentRequestDto;
import ibrel.tgBeautyWebApp.dto.booking.VariableServiceSelectionDto;
import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.booking.AppointmentStatus;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.*;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final MasterRepository masterRepository;
    private final WorkSlotRepository workSlotRepository;
    private final MasterServiceWorkRepository masterServiceWorkRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;
    private final EntityManager entityManager;

    private static final Set<String> ACTIVE_STATUSES = Set.of(AppointmentStatus.BOOKED.name(), AppointmentStatus.CONFIRMED.name());

    @Override
    @Transactional
    public Appointment create(AppointmentRequestDto request) {
        Assert.notNull(request, "request must not be null");
        Assert.notNull(request.getUserTelegramId(), "userTelegramId must not be null");
        Assert.notNull(request.getMasterId(), "masterId must not be null");
        Assert.notNull(request.getSlotId(), "slotId must not be null");
        Assert.notEmpty(request.getServiceIds(), "serviceIds must not be empty");

        // 1) Загрузка пользователя по telegramId (явная семантика)
        UserTG user = userRepository.findByTelegramId(request.getUserTelegramId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден telegramId=" + request.getUserTelegramId()));

        // 2) Проверка активности пользователя
        if (!Boolean.TRUE.equals(user.getActive())) {
            log.warn("Inactive user attempted booking: telegramId={}", request.getUserTelegramId());
            throw new IllegalStateException("Пользователь не активен");
        }

        // 3) Проверка существования мастера
        masterRepository.findById(request.getMasterId())
                .orElseThrow(() -> new EntityNotFoundException("Мастер не найден id=" + request.getMasterId()));

        // 4) Пессимистическая блокировка слота
        WorkSlot slot = entityManager.find(WorkSlot.class, request.getSlotId(), LockModeType.PESSIMISTIC_WRITE);
        if (slot == null) {
            throw new EntityNotFoundException("Слот не найден id=" + request.getSlotId());
        }
        if (slot.getMaster() == null || !slot.getMaster().getId().equals(request.getMasterId())) {
            log.warn("Slot {} does not belong to master {}", request.getSlotId(), request.getMasterId());
            throw new IllegalArgumentException("Слот не принадлежит указанному мастеру");
        }

        // 5) Быстрая проверка занятости слота
        boolean occupied = appointmentRepository.existsBySlotIdAndStatusIn(slot.getId(), ACTIVE_STATUSES);
        if (occupied) {
            log.warn("Attempt to book already occupied slot id={}", slot.getId());
            throw new IllegalStateException("Слот уже занят");
        }

        // 6) Загрузка услуг и валидация
        List<MasterServiceWork> services = masterServiceWorkRepository.findAllById(request.getServiceIds());
        if (services.size() != request.getServiceIds().size()) {
            List<Long> found = services.stream().map(MasterServiceWork::getId).collect(Collectors.toList());
            List<Long> missing = request.getServiceIds().stream().filter(id -> !found.contains(id)).collect(Collectors.toList());
            throw new EntityNotFoundException("Не найдены услуги: " + missing);
        }

        // 7) Подготовка map для переменных параметров
        Map<Long, VariableServiceSelectionDto> variableMap = Optional.ofNullable(request.getVariableSelections())
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(VariableServiceSelectionDto::getServiceId, v -> v, (a, b) -> a));

        int totalDuration = 0;
        double totalPrice = 0.0;

        for (MasterServiceWork s : services) {
            if (s.getType() == null) {
                log.warn("Service id={} has null type", s.getId());
                throw new IllegalArgumentException("Тип услуги не указан id=" + s.getId());
            }
            switch (s.getType()) {
                case FIXED -> {
                    FixedServiceDetails fd = s.getFixedDetails();
                    if (fd == null) throw new IllegalArgumentException("У фиксированной услуги отсутствуют детали id=" + s.getId());
                    if (fd.getDurationMinutes() == null || fd.getDurationMinutes() <= 0)
                        throw new IllegalArgumentException("Неверная длительность для услуги id=" + s.getId());
                    if (fd.getPrice() == null || fd.getPrice() < 0)
                        throw new IllegalArgumentException("Неверная цена для услуги id=" + s.getId());
                    totalDuration += fd.getDurationMinutes();
                    totalPrice += fd.getPrice();
                }
                case VARIABLE -> {
                    VariableServiceSelectionDto selection = variableMap.get(s.getId());
                    if (selection == null) {
                        log.warn("Variable service id={} requires parameters", s.getId());
                        throw new IllegalArgumentException("Для вариативной услуги требуются параметры id=" + s.getId());
                    }
                    List<VariableServiceDetails> candidates = variableServiceDetailsRepository.findByServiceId(s.getId());
                    Optional<VariableServiceDetails> matched = candidates.stream()
                            .filter(vd -> selection.getFactorName() != null && selection.getFactorValue() != null)
                            .filter(vd -> selection.getFactorName().equalsIgnoreCase(vd.getFactorName())
                                    && selection.getFactorValue().equalsIgnoreCase(vd.getFactorValue()))
                            .findFirst();

                    if (matched.isEmpty()) {
                        log.warn("No matching variable detail for serviceId={} factor={} value={}", s.getId(), selection.getFactorName(), selection.getFactorValue());
                        throw new IllegalArgumentException("Нет подходящего варианта для услуги id=" + s.getId());
                    }
                    VariableServiceDetails vd = matched.get();
                    if (vd.getDurationMinutes() == null || vd.getDurationMinutes() <= 0)
                        throw new IllegalArgumentException("Неверная длительность для вариативной опции id=" + vd.getId());
                    if (vd.getPrice() == null || vd.getPrice() < 0)
                        throw new IllegalArgumentException("Неверная цена для вариативной опции id=" + vd.getId());
                    totalDuration += vd.getDurationMinutes();
                    totalPrice += vd.getPrice();
                }
                default -> throw new IllegalArgumentException("Неподдерживаемый тип услуги id=" + s.getId());
            }
        }

        // 8) Проверка вместимости слота
        long slotMinutes = ChronoUnit.MINUTES.between(slot.getStartTime(), slot.getEndTime());
        if (totalDuration > slotMinutes) {
            log.warn("Total duration {} exceeds slot length {} for slot id={}", totalDuration, slotMinutes, slot.getId());
            throw new IllegalStateException("Суммарная длительность услуг не помещается в слот");
        }

        // 9) Финальная проверка пересечения (защита от гонок)
        boolean overlapping = appointmentRepository.findBySlotIdAndStatusIn(slot.getId(), ACTIVE_STATUSES).stream()
                .anyMatch(a -> a.getSlot() != null && a.getSlot().getId().equals(slot.getId()));
        if (overlapping) {
            log.warn("Concurrent booking detected for slot id={}", slot.getId());
            throw new IllegalStateException("Слот уже занят (конкурентная бронь)");
        }

        // 10) Сборка и сохранение записи
        Appointment appointment = Appointment.builder()
                .user(user)
                .master(slot.getMaster())
                .slot(slot)
                .services(services)
                .totalDuration(totalDuration)
                .totalPrice(totalPrice)
                .status(AppointmentStatus.BOOKED.name())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Создана запись id={} telegramUser={} masterId={} slotId={}", saved.getId(), user.getTelegramId(), slot.getMaster().getId(), slot.getId());
        return saved;
    }

    @Override
    public Appointment getById(Long id) {
        Assert.notNull(id, "id must not be null");
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found id=" + id));
    }

    @Override
    public List<Appointment> getByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));
        return appointmentRepository.findByMasterId(masterId);
    }

    @Override
    public List<Appointment> getByUser(Long userId) {
        Assert.notNull(userId, "userId must not be null");
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));
        return appointmentRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Appointment cancel(Long appointmentId, Long requestedByUserId) {
        Assert.notNull(appointmentId, "appointmentId must not be null");
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found id=" + appointmentId));

        if (requestedByUserId != null && !appt.getUser().getId().equals(requestedByUserId)) {
            log.warn("User {} attempted to cancel appointment {} owned by {}", requestedByUserId, appointmentId, appt.getUser().getId());
            throw new IllegalArgumentException("Пользователь не может отменить эту запись");
        }

        if (AppointmentStatus.CANCELLED.name().equals(appt.getStatus())) {
            log.debug("Appointment id={} already cancelled", appointmentId);
            return appt;
        }

        appt.setStatus(AppointmentStatus.CANCELLED.name());
        Appointment saved = appointmentRepository.save(appt);
        log.info("Cancelled appointment id={}", appointmentId);
        return saved;
    }

    @Override
    @Transactional
    public Appointment complete(Long appointmentId) {
        Assert.notNull(appointmentId, "appointmentId must not be null");
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found id=" + appointmentId));

        if (AppointmentStatus.COMPLETED.name().equals(appt.getStatus())) {
            log.debug("Appointment id={} already completed", appointmentId);
            return appt;
        }

        appt.setStatus(AppointmentStatus.COMPLETED.name());
        Appointment saved = appointmentRepository.save(appt);
        log.info("Completed appointment id={}", appointmentId);
        return saved;
    }
}
