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
        Assert.notNull(request.getUserId(), "userId must not be null");
        Assert.notNull(request.getMasterId(), "masterId must not be null");
        Assert.notNull(request.getSlotId(), "slotId must not be null");
        Assert.notEmpty(request.getServiceIds(), "serviceIds must not be empty");

        UserTG user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found id=" + request.getUserId()));

        masterRepository.findById(request.getMasterId())
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + request.getMasterId()));

        // Блокируем слот для записи (пессимистическая блокировка)
        WorkSlot slot = entityManager.find(WorkSlot.class, request.getSlotId(), LockModeType.PESSIMISTIC_WRITE);
        if (slot == null) throw new EntityNotFoundException("Slot not found id=" + request.getSlotId());
        if (!slot.getMaster().getId().equals(request.getMasterId())) {
            log.warn("Slot {} does not belong to master {}", request.getSlotId(), request.getMasterId());
            throw new IllegalArgumentException("Slot does not belong to master");
        }

        // Проверка занятости слота
        boolean occupied = appointmentRepository.existsBySlotIdAndStatusIn(slot.getId(), ACTIVE_STATUSES);
        if (occupied) {
            log.warn("Slot id={} already occupied", slot.getId());
            throw new IllegalStateException("Slot is already booked");
        }

        // Получаем услуги
        List<MasterServiceWork> services = masterServiceWorkRepository.findAllById(request.getServiceIds());
        if (services.size() != request.getServiceIds().size()) {
            List<Long> found = services.stream().map(MasterServiceWork::getId).collect(Collectors.toList());
            List<Long> missing = request.getServiceIds().stream().filter(id -> !found.contains(id)).collect(Collectors.toList());
            throw new EntityNotFoundException("Some services not found: " + missing);
        }

        // Подготовка map для VARIABLE параметров
        Map<Long, VariableServiceSelectionDto> variableMap = Optional.ofNullable(request.getVariableSelections())
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(VariableServiceSelectionDto::getServiceId, v -> v));

        int totalDuration = 0;
        double totalPrice = 0.0;

        for (MasterServiceWork s : services) {
            if (s.getType() == null) {
                log.warn("Service id={} has null type", s.getId());
                throw new IllegalArgumentException("Service type is not specified for id=" + s.getId());
            }
            switch (s.getType()) {
                case FIXED -> {
                    FixedServiceDetails fd = s.getFixedDetails();
                    if (fd == null) throw new IllegalArgumentException("Fixed service missing details id=" + s.getId());
                    if (fd.getDurationMinutes() == null || fd.getDurationMinutes() <= 0)
                        throw new IllegalArgumentException("Invalid duration for service id=" + s.getId());
                    if (fd.getPrice() == null || fd.getPrice() < 0)
                        throw new IllegalArgumentException("Invalid price for service id=" + s.getId());
                    totalDuration += fd.getDurationMinutes();
                    totalPrice += fd.getPrice();
                }
                case VARIABLE -> {
                    VariableServiceSelectionDto selection = variableMap.get(s.getId());
                    if (selection == null) {
                        log.warn("Variable service id={} requires parameters", s.getId());
                        throw new IllegalArgumentException("Variable service requires parameters id=" + s.getId());
                    }
                    // Стратегия: ищем VariableServiceDetails по factorName/factorValue
                    List<VariableServiceDetails> candidates = variableServiceDetailsRepository.findByServiceId(s.getId());
                    Optional<VariableServiceDetails> matched = candidates.stream()
                            .filter(vd -> selection.getFactorName() != null && selection.getFactorValue() != null)
                            .filter(vd -> selection.getFactorName().equalsIgnoreCase(vd.getFactorName())
                                    && selection.getFactorValue().equalsIgnoreCase(vd.getFactorValue()))
                            .findFirst();

                    if (matched.isEmpty()) {
                        log.warn("No matching variable detail for serviceId={} factor={} value={}", s.getId(), selection.getFactorName(), selection.getFactorValue());
                        throw new IllegalArgumentException("No matching variable option for service id=" + s.getId());
                    }
                    VariableServiceDetails vd = matched.get();
                    if (vd.getDurationMinutes() == null || vd.getDurationMinutes() <= 0)
                        throw new IllegalArgumentException("Invalid duration for variable detail id=" + vd.getId());
                    if (vd.getPrice() == null || vd.getPrice() < 0)
                        throw new IllegalArgumentException("Invalid price for variable detail id=" + vd.getId());
                    totalDuration += vd.getDurationMinutes();
                    totalPrice += vd.getPrice();
                }
                default -> throw new IllegalArgumentException("Unsupported service type for id=" + s.getId());
            }
        }

        long slotMinutes = ChronoUnit.MINUTES.between(slot.getStartTime(), slot.getEndTime());
        if (totalDuration > slotMinutes) {
            log.warn("Total duration {} exceeds slot length {} for slot id={}", totalDuration, slotMinutes, slot.getId());
            throw new IllegalStateException("Selected services total duration does not fit into the slot");
        }

        // Финальная проверка пересечения (на всякий случай)
        boolean overlapping = appointmentRepository.findBySlotIdAndStatusIn(slot.getId(), ACTIVE_STATUSES).stream()
                .anyMatch(a -> a.getSlot() != null && a.getSlot().getId().equals(slot.getId()));
        if (overlapping) {
            log.warn("Concurrent booking detected for slot id={}", slot.getId());
            throw new IllegalStateException("Slot is already booked");
        }

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
        log.info("Created appointment id={} userId={} masterId={} slotId={}", saved.getId(), user.getId(), slot.getMaster().getId(), slot.getId());
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
            throw new IllegalArgumentException("User not allowed to cancel this appointment");
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
