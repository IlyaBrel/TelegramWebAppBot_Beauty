package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.repository.WorkSlotRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.service.master.WorkSlotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSlotServiceImpl implements WorkSlotService {

    private final WorkSlotRepository workSlotRepository;
    private final MasterRepository masterRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public WorkSlot create(Long masterId, WorkSlot slot) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(slot, "slot must not be null");
        Assert.notNull(slot.getStartTime(), "slot.startTime must not be null");
        Assert.notNull(slot.getEndTime(), "slot.endTime must not be null");
        Assert.hasText(slot.getDayOfWeek(), "slot.dayOfWeek must not be empty");

        if (!slot.getStartTime().isBefore(slot.getEndTime())) {
            log.warn("Invalid slot times: start >= end ({} >= {})", slot.getStartTime(), slot.getEndTime());
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        // Проверка пересечения с существующими слотами мастера
        List<WorkSlot> existing = workSlotRepository.findByMasterId(masterId);
        for (WorkSlot s : existing) {
            if (!s.getDayOfWeek().equalsIgnoreCase(slot.getDayOfWeek())) continue;
            // допускается касание: s.end == slot.start или s.start == slot.end
            boolean overlap = !(slot.getEndTime().isBefore(s.getStartTime()) || slot.getStartTime().isAfter(s.getEndTime()));
            // если слот полностью совпадает по границам, это тоже overlap
            if (overlap && !(slot.getEndTime().equals(s.getStartTime()) || slot.getStartTime().equals(s.getEndTime()))) {
                log.warn("New slot overlaps with existing slot id={} for master id={}", s.getId(), masterId);
                throw new IllegalArgumentException("New slot overlaps with existing slot");
            }
        }

        slot.setMaster(master);
        WorkSlot saved = workSlotRepository.save(slot);
        log.info("Created work slot id={} masterId={} {} {}-{}", saved.getId(), masterId, saved.getDayOfWeek(),
                saved.getStartTime(), saved.getEndTime());
        return saved;
    }

    @Override
    @Transactional
    public WorkSlot update(Long slotId, WorkSlot slot) {
        Assert.notNull(slotId, "slotId must not be null");
        Assert.notNull(slot, "slot must not be null");
        WorkSlot existing = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("WorkSlot not found id=" + slotId));

        LocalTime newStart = slot.getStartTime() != null ? slot.getStartTime() : existing.getStartTime();
        LocalTime newEnd = slot.getEndTime() != null ? slot.getEndTime() : existing.getEndTime();
        String newDay = slot.getDayOfWeek() != null ? slot.getDayOfWeek() : existing.getDayOfWeek();

        if (!newStart.isBefore(newEnd)) {
            log.warn("Invalid updated slot times: start >= end ({} >= {})", newStart, newEnd);
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Проверка пересечения с другими слотами мастера (исключая текущий)
        Long masterId = existing.getMaster().getId();
        List<WorkSlot> otherSlots = workSlotRepository.findByMasterId(masterId).stream()
                .filter(s -> !s.getId().equals(slotId))
                .collect(Collectors.toList());

        for (WorkSlot s : otherSlots) {
            if (!s.getDayOfWeek().equalsIgnoreCase(newDay)) continue;
            boolean overlap = !(newEnd.isBefore(s.getStartTime()) || newStart.isAfter(s.getEndTime()));
            if (overlap && !(newEnd.equals(s.getStartTime()) || newStart.equals(s.getEndTime()))) {
                log.warn("Updated slot would overlap with slot id={} for master id={}", s.getId(), masterId);
                throw new IllegalArgumentException("Updated slot overlaps with existing slot");
            }
        }

        existing.setDayOfWeek(newDay);
        existing.setStartTime(newStart);
        existing.setEndTime(newEnd);
        WorkSlot saved = workSlotRepository.save(existing);
        log.info("Updated work slot id={} masterId={} {} {}-{}", saved.getId(), masterId, saved.getDayOfWeek(),
                saved.getStartTime(), saved.getEndTime());
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long slotId) {
        Assert.notNull(slotId, "slotId must not be null");
        WorkSlot slot = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("WorkSlot not found id=" + slotId));

        // Проверка: есть ли будущие записи в этом слоте
        boolean hasFutureAppointments = appointmentRepository.findByMasterId(slot.getMaster().getId()).stream()
                .filter(a -> a.getSlot() != null && a.getSlot().getId().equals(slotId))
                .anyMatch(a -> !"CANCELLED".equalsIgnoreCase(a.getStatus()) && a.getCreatedAt() != null);

        if (hasFutureAppointments) {
            log.warn("Attempt to delete slot id={} with existing appointments", slotId);
            throw new IllegalStateException("Cannot delete slot with existing appointments");
        }

        workSlotRepository.deleteById(slotId);
        log.info("Deleted work slot id={}", slotId);
    }

    @Override
    public List<WorkSlot> findByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));
        return workSlotRepository.findByMasterId(masterId);
    }

    @Override
    public List<WorkSlot> findAvailable(Long masterId, LocalDate date) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(date, "date must not be null");

        masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        DayOfWeek dow = date.getDayOfWeek();
        String dayName = dow.name(); // MONDAY..SUNDAY

        // Получаем все слоты мастера для дня недели
        List<WorkSlot> slots = workSlotRepository.findByMasterId(masterId).stream()
                .filter(s -> s.getDayOfWeek() != null && s.getDayOfWeek().equalsIgnoreCase(dayName))
                .collect(Collectors.toList());

        // Исключаем слоты, в которых уже есть активные записи (BOOKED/CONFIRMED)
        List<Long> occupiedSlotIds = appointmentRepository.findByMasterId(masterId).stream()
                .filter(a -> a.getSlot() != null)
                .filter(a -> {
                    String st = a.getStatus();
                    return st != null && (st.equalsIgnoreCase("BOOKED") || st.equalsIgnoreCase("CONFIRMED"));
                })
                .map(a -> a.getSlot().getId())
                .collect(Collectors.toList());

        List<WorkSlot> available = slots.stream()
                .filter(s -> !occupiedSlotIds.contains(s.getId()))
                .collect(Collectors.toList());

        log.debug("Found {} available slots for masterId={} date={}", available.size(), masterId, date);
        return available;
    }
}
