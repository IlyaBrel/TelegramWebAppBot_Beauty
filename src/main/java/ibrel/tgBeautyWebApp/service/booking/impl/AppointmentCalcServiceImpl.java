package ibrel.tgBeautyWebApp.service.booking.impl;

import ibrel.tgBeautyWebApp.dto.booking.*;
import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.*;
import ibrel.tgBeautyWebApp.service.booking.AppointmentCalcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentCalcServiceImpl implements AppointmentCalcService {

    private final MasterRepository masterRepository;
    private final WorkSlotRepository workSlotRepository;
    private final MasterServiceWorkRepository masterServiceWorkRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public AppointmentCalcResponseDto calculate(AppointmentCalcRequestDto req) {
        CalculationResult r = calculateInternal(req.getMasterId(), req.getSlotId(), req.getServices());
        return AppointmentCalcResponseDto.builder()
                .totalDurationMinutes(r.totalDuration)
                .totalPrice(r.totalPrice)
                .fitsInSlot(r.fitsInSlot)
                .build();
    }

    @Override
    public AppointmentAvailabilityResponseDto checkAvailability(AppointmentCalcRequestDto req) {
        Assert.notNull(req, "request must not be null");
        // basic existence checks
        if (!masterRepository.existsById(req.getMasterId())) {
            throw new EntityNotFoundException("Master not found id=" + req.getMasterId());
        }
        WorkSlot slot = workSlotRepository.findById(req.getSlotId())
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + req.getSlotId()));
        if (!slot.getMaster().getId().equals(req.getMasterId())) {
            return AppointmentAvailabilityResponseDto.builder()
                    .available(false).slotFree(false).fitsInSlot(false)
                    .reason("Slot does not belong to master").build();
        }

        boolean slotFree = !appointmentRepository.existsBySlot_Id(req.getSlotId());
        CalculationResult r = calculateInternal(req.getMasterId(), req.getSlotId(), req.getServices());

        boolean available = slotFree && r.fitsInSlot;
        String reason = null;
        if (!slotFree) reason = "Slot already booked";
        else if (!r.fitsInSlot) reason = "Total duration exceeds slot length";

        return AppointmentAvailabilityResponseDto.builder()
                .available(available)
                .slotFree(slotFree)
                .fitsInSlot(r.fitsInSlot)
                .reason(reason)
                .build();
    }

    @Override
    public List<WorkSlotShortDto> findNearestSlots(NearestSlotsRequestDto request) {
        Assert.notNull(request, "request must not be null");
        Long masterId = request.getMasterId();
        int limit = Optional.ofNullable(request.getLimit()).orElse(5);

        if (!masterRepository.existsById(masterId)) {
            throw new EntityNotFoundException("Master not found id=" + masterId);
        }

        // Получаем все слоты мастера
        List<WorkSlot> slots = workSlotRepository.findByMasterId(masterId);
        // Сортируем по ближайшему времени относительно today (день недели + startTime)
        LocalDate today = LocalDate.now();
        DayOfWeek todayDow = today.getDayOfWeek();

        List<WorkSlotWithNextOccurrence> withOcc = slots.stream()
                .map(s -> new WorkSlotWithNextOccurrence(s, nextOccurrenceDateForDay(s.getDayOfWeek(), today)))
                .filter(w -> w.nextDate != null)
                .sorted(Comparator.comparing(w -> w.nextDate.atTime(w.slot.getStartTime())))
                .collect(Collectors.toList());

        List<WorkSlotShortDto> result = new ArrayList<>();
        for (WorkSlotWithNextOccurrence w : withOcc) {
            if (result.size() >= limit) break;
            // Проверяем, свободен ли слот и помещаются ли услуги
            boolean slotFree = !appointmentRepository.existsBySlot_Id(w.slot.getId());
            CalculationResult calc = calculateInternal(masterId, w.slot.getId(), request.getServices());
            if (slotFree && calc.fitsInSlot) {
                result.add(WorkSlotShortDto.builder()
                        .id(w.slot.getId())
                        .dayOfWeek(w.slot.getDayOfWeek())
                        .startTime(w.slot.getStartTime())
                        .endTime(w.slot.getEndTime())
                        .build());
            }
        }
        return result;
    }

    @Override
    public OptimalSlotResponseDto findOptimalSlot(NearestSlotsRequestDto request) {
        Assert.notNull(request, "request must not be null");
        List<WorkSlotShortDto> nearest = findNearestSlots(request);
        if (nearest.isEmpty()) {
            return OptimalSlotResponseDto.builder().slot(null).fitsInSlot(false).build();
        }
        // Выбираем первый (самый ранний) как оптимальный
        WorkSlotShortDto chosen = nearest.get(0);
        return OptimalSlotResponseDto.builder().slot(chosen).fitsInSlot(true).build();
    }

    // ---- вспомогательные структуры и методы ----

    private static class CalculationResult {
        int totalDuration;
        double totalPrice;
        boolean fitsInSlot;
    }

    private static class WorkSlotWithNextOccurrence {
        WorkSlot slot;
        LocalDate nextDate;
        WorkSlotWithNextOccurrence(WorkSlot slot, LocalDate nextDate) { this.slot = slot; this.nextDate = nextDate; }
    }

    private CalculationResult calculateInternal(Long masterId, Long slotId, List<ServiceSelectionDto> services) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(slotId, "slotId must not be null");
        Assert.notNull(services, "services must not be null");

        WorkSlot slot = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + slotId));

        if (!slot.getMaster().getId().equals(masterId)) {
            throw new IllegalArgumentException("Slot does not belong to master");
        }

        int totalDuration = 0;
        double totalPrice = 0.0;

        for (ServiceSelectionDto sel : services) {
            MasterServiceWork service = masterServiceWorkRepository.findById(sel.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + sel.getServiceId()));

            if (!service.getMaster().getId().equals(masterId)) {
                throw new IllegalArgumentException("Service id=" + sel.getServiceId() + " does not belong to master");
            }

            if (service.getType() != null && service.getType().name().equals("FIXED")) {
                if (service.getFixedDetails() == null) {
                    throw new IllegalArgumentException("Fixed service has no details");
                }
                totalDuration += Optional.ofNullable(service.getFixedDetails().getDurationMinutes()).orElse(0);
                totalPrice += Optional.ofNullable(service.getFixedDetails().getPrice()).orElse(0.0);
            } else {
                if (sel.getVariableDetailIds() == null || sel.getVariableDetailIds().isEmpty()) {
                    throw new IllegalArgumentException("Variable service requires variable details");
                }
                for (Long varId : sel.getVariableDetailIds()) {
                    VariableServiceDetails v = variableServiceDetailsRepository.findById(varId)
                            .orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + varId));
                    if (!v.getService().getId().equals(service.getId())) {
                        throw new IllegalArgumentException("Variable detail id=" + varId + " does not belong to service id=" + service.getId());
                    }
                    totalDuration += Optional.ofNullable(v.getDurationMinutes()).orElse(0);
                    totalPrice += Optional.ofNullable(v.getPrice()).orElse(0.0);
                }
            }
        }

        long slotMinutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        boolean fits = totalDuration <= slotMinutes;

        CalculationResult r = new CalculationResult();
        r.totalDuration = totalDuration;
        r.totalPrice = totalPrice;
        r.fitsInSlot = fits;
        return r;
    }

    /**
     * Преобразует строку dayOfWeek (MONDAY...) в ближайшую дату >= today с этим днем недели.
     * Если dayOfWeek некорректен — возвращает null.
     */
    private LocalDate nextOccurrenceDateForDay(String dayOfWeekStr, LocalDate today) {
        try {
            DayOfWeek dow = DayOfWeek.valueOf(dayOfWeekStr);
            int daysUntil = (dow.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
            return today.plusDays(daysUntil);
        } catch (Exception ex) {
            return null;
        }
    }
}
