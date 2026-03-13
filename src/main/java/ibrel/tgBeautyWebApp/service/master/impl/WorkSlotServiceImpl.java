package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentSlotRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.master.WorkSlotRepository;
import ibrel.tgBeautyWebApp.service.master.WorkSlotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSlotServiceImpl implements WorkSlotService {

    private final MasterRepository masterRepository;
    private final WorkSlotRepository workSlotRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;

    @Override
    @Transactional
    public WorkSlot create(Long masterId, WorkSlot slot) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(slot, "slot must not be null");
        Assert.notNull(slot.getDayOfWeek(), "slot.dayOfWeek must not be null");
        Assert.notNull(slot.getStartTime(), "slot.startTime must not be null");
        Assert.notNull(slot.getEndTime(), "slot.endTime must not be null");

        if (!slot.getEndTime().isAfter(slot.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        validateDayOfWeek(slot.getDayOfWeek());

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        List<WorkSlot> existing = workSlotRepository.findByMaster_IdAndDayOfWeek(masterId, slot.getDayOfWeek());
        checkOverlap(slot, existing, null);

        slot.setMaster(master);
        WorkSlot saved = workSlotRepository.save(slot);
        log.info("Created work slot id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public WorkSlot update(Long slotId, WorkSlot slot) {
        Assert.notNull(slotId, "slotId must not be null");
        Assert.notNull(slot, "slot must not be null");
        Assert.notNull(slot.getDayOfWeek(), "slot.dayOfWeek must not be null");
        Assert.notNull(slot.getStartTime(), "slot.startTime must not be null");
        Assert.notNull(slot.getEndTime(), "slot.endTime must not be null");

        if (!slot.getEndTime().isAfter(slot.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        WorkSlot existing = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + slotId));

        Long masterId = existing.getMaster() != null ? existing.getMaster().getId() : null;
        if (masterId == null) throw new IllegalStateException("Slot has no master assigned");

        // Нельзя менять время/день если слот занят активной записью
        boolean hasActive = appointmentSlotRepository.existsBySlotIdAndActive(slotId,
                Appointment.Status.CANCELLED,
                Appointment.Status.REJECTED);
        if (hasActive) {
            boolean timeChanged = !slot.getStartTime().equals(existing.getStartTime())
                    || !slot.getEndTime().equals(existing.getEndTime())
                    || !slot.getDayOfWeek().equals(existing.getDayOfWeek());
            if (timeChanged) {
                throw new IllegalStateException(
                        "Cannot change slot time/day: active appointments exist on this slot");
            }
        }

        List<WorkSlot> others = workSlotRepository.findByMaster_IdAndDayOfWeek(masterId, slot.getDayOfWeek());
        checkOverlap(slot, others, slotId);

        existing.setDayOfWeek(slot.getDayOfWeek());
        existing.setStartTime(slot.getStartTime());
        existing.setEndTime(slot.getEndTime());
        existing.setNote(slot.getNote());

        WorkSlot saved = workSlotRepository.save(existing);
        log.info("Updated work slot id={}", slotId);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long slotId) {
        Assert.notNull(slotId, "slotId must not be null");
        workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + slotId));

        if (appointmentSlotRepository.existsBySlotIdAndActive(slotId,
                Appointment.Status.CANCELLED,
                Appointment.Status.REJECTED)) {
            throw new IllegalStateException("Cannot delete slot with active appointments");
        }

        workSlotRepository.deleteById(slotId);
        log.info("Deleted work slot id={}", slotId);
    }

    @Override
    public List<WorkSlot> findByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId))
            throw new EntityNotFoundException("Master not found id=" + masterId);
        return workSlotRepository.findByMaster_IdOrderByDayOfWeekAscStartTimeAsc(masterId);
    }

    /**
     * Свободные слоты мастера на конкретный день недели.
     * findAvailable(Long, LocalDate) удалён — нигде не вызывался.
     * Единственный потребитель нужного функционала — AppointmentCalcService —
     * работает с workSlotRepository напрямую.
     */
    @Override
    public List<WorkSlot> findFree(Long masterId, String dayOfWeek) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(dayOfWeek, "dayOfWeek must not be null");
        validateDayOfWeek(dayOfWeek);
        if (!masterRepository.existsById(masterId))
            throw new EntityNotFoundException("Master not found id=" + masterId);
        return workSlotRepository.findFreeSlots(masterId, dayOfWeek);
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    private void checkOverlap(WorkSlot newSlot, List<WorkSlot> existing, Long excludeSlotId) {
        for (WorkSlot s : existing) {
            if (excludeSlotId != null && s.getId().equals(excludeSlotId)) continue;
            boolean overlap = !(newSlot.getEndTime().isBefore(s.getStartTime())
                    || newSlot.getStartTime().isAfter(s.getEndTime()));
            if (overlap) {
                throw new IllegalArgumentException(
                        "Slot overlaps with existing slot id=" + s.getId()
                                + " [" + s.getStartTime() + " - " + s.getEndTime() + "]");
            }
        }
    }

    private void validateDayOfWeek(String day) {
        try {
            DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid dayOfWeek: " + day
                    + ". Must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
        }
    }
}