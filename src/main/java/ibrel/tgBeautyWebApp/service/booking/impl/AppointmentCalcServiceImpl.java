package ibrel.tgBeautyWebApp.service.booking.impl;

import ibrel.tgBeautyWebApp.dto.booking.*;
import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentSlotRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterServiceWorkRepository;
import ibrel.tgBeautyWebApp.repository.master.VariableServiceDetailsRepository;
import ibrel.tgBeautyWebApp.repository.master.WorkSlotRepository;
import ibrel.tgBeautyWebApp.service.booking.AppointmentCalcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentCalcServiceImpl implements AppointmentCalcService {

    private final MasterRepository masterRepository;
    private final WorkSlotRepository workSlotRepository;
    private final MasterServiceWorkRepository masterServiceWorkRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;

    @Override
    public AppointmentCalcResponseDto calculate(AppointmentCalcRequestDto req) {
        CalcResult r = calcInternal(req.getMasterId(), req.getSlotId(), req.getServices());
        return AppointmentCalcResponseDto.builder()
                .totalDurationMinutes(r.totalDuration)
                .totalPrice(r.totalPrice)
                .fitsInSlot(r.fitsInSlot)
                .build();
    }

    @Override
    public AppointmentAvailabilityResponseDto checkAvailability(AppointmentCalcRequestDto req) {
        Assert.notNull(req, "request must not be null");

        if (!masterRepository.existsById(req.getMasterId()))
            throw new EntityNotFoundException("Master not found id=" + req.getMasterId());

        WorkSlot slot = workSlotRepository.findById(req.getSlotId())
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + req.getSlotId()));

        if (!slot.getMaster().getId().equals(req.getMasterId())) {
            return AppointmentAvailabilityResponseDto.builder()
                    .available(false).slotFree(false).fitsInSlot(false)
                    .reason("Slot does not belong to master").build();
        }

        boolean slotFree = !appointmentSlotRepository.existsBySlotIdAndActive(
                req.getSlotId(),
                Appointment.Status.CANCELLED,
                Appointment.Status.REJECTED);
        CalcResult r = calcInternal(req.getMasterId(), req.getSlotId(), req.getServices());

        boolean available = slotFree && r.fitsInSlot;
        String reason = null;
        if (!slotFree) reason = "Slot already booked";
        else if (!r.fitsInSlot) reason = "Total duration exceeds slot length";

        return AppointmentAvailabilityResponseDto.builder()
                .available(available).slotFree(slotFree).fitsInSlot(r.fitsInSlot).reason(reason)
                .build();
    }

    @Override
    public List<WorkSlotShortDto> findNearestSlots(NearestSlotsRequestDto req) {
        Assert.notNull(req, "request must not be null");
        int limit = Optional.ofNullable(req.getLimit()).orElse(5);

        if (!masterRepository.existsById(req.getMasterId()))
            throw new EntityNotFoundException("Master not found id=" + req.getMasterId());

        List<WorkSlot> allSlots = workSlotRepository.findByMaster_IdOrderByDayOfWeekAscStartTimeAsc(req.getMasterId());
        LocalDate today = LocalDate.now();

        List<WorkSlotWithDate> withDates = allSlots.stream()
                .map(s -> new WorkSlotWithDate(s, nextDate(s.getDayOfWeek(), today)))
                .filter(w -> w.date != null)
                .sorted(Comparator.comparing(w -> w.date.atTime(w.slot.getStartTime())))
                .collect(Collectors.toList());

        List<WorkSlotShortDto> result = new ArrayList<>();
        for (WorkSlotWithDate w : withDates) {
            if (result.size() >= limit) break;
            boolean free = !appointmentSlotRepository.existsBySlotIdAndActive(w.slot.getId(),
                    Appointment.Status.CANCELLED,
                    Appointment.Status.REJECTED);
            if (!free) continue;
            CalcResult calc = calcInternal(req.getMasterId(), w.slot.getId(), req.getServices());
            if (calc.fitsInSlot) {
                result.add(toShortDto(w.slot));
            }
        }
        return result;
    }

    @Override
    public OptimalSlotResponseDto findOptimalSlot(NearestSlotsRequestDto req) {
        List<WorkSlotShortDto> nearest = findNearestSlots(req);
        if (nearest.isEmpty())
            return OptimalSlotResponseDto.builder().slot(null).fitsInSlot(false).build();
        return OptimalSlotResponseDto.builder().slot(nearest.get(0)).fitsInSlot(true).build();
    }

    // ── Внутренние методы ─────────────────────────────────────────────────────

    private CalcResult calcInternal(Long masterId, Long slotId, List<ServiceSelectionDto> services) {
        WorkSlot slot = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + slotId));

        if (!slot.getMaster().getId().equals(masterId))
            throw new IllegalArgumentException("Slot does not belong to master");

        int totalDuration = 0;
        double totalPrice = 0.0;

        for (ServiceSelectionDto sel : services) {
            MasterServiceWork service = masterServiceWorkRepository.findById(sel.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + sel.getServiceId()));

            if (!service.getMaster().getId().equals(masterId))
                throw new IllegalArgumentException("Service does not belong to master");

            switch (service.getType()) {
                case FIXED -> {
                    if (service.getFixedDetails() == null)
                        throw new IllegalArgumentException("Fixed service has no details");
                    totalDuration += Optional.ofNullable(service.getFixedDetails().getDurationMinutes()).orElse(0);
                    totalPrice += Optional.ofNullable(service.getFixedDetails().getPrice()).orElse(0.0);
                }
                case VARIABLE -> {
                    if (sel.getVariableDetailIds() == null || sel.getVariableDetailIds().isEmpty())
                        throw new IllegalArgumentException("Variable service requires variable detail ids");
                    for (Long varId : sel.getVariableDetailIds()) {
                        VariableServiceDetails v = variableServiceDetailsRepository.findById(varId)
                                .orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + varId));
                        if (!v.getService().getId().equals(service.getId()))
                            throw new IllegalArgumentException("Variable detail does not belong to service");
                        totalDuration += Optional.ofNullable(v.getDurationMinutes()).orElse(0);
                        totalPrice += Optional.ofNullable(v.getPrice()).orElse(0.0);
                    }
                }
            }
        }

        long slotMinutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        return new CalcResult(totalDuration, totalPrice, totalDuration <= slotMinutes);
    }

    private LocalDate nextDate(String dayOfWeekStr, LocalDate today) {
        try {
            DayOfWeek dow = DayOfWeek.valueOf(dayOfWeekStr);
            int delta = (dow.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
            return today.plusDays(delta);
        } catch (Exception e) {
            return null;
        }
    }

    private WorkSlotShortDto toShortDto(WorkSlot s) {
        return WorkSlotShortDto.builder()
                .id(s.getId())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .build();
    }

    private record CalcResult(int totalDuration, double totalPrice, boolean fitsInSlot) {
    }

    private record WorkSlotWithDate(WorkSlot slot, LocalDate date) {
    }
}