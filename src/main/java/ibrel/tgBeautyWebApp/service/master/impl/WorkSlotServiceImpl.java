package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.WorkSlotRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSlotServiceImpl implements WorkSlotService {

    private final MasterRepository masterRepository;
    private final WorkSlotRepository workSlotRepository;
    private final AppointmentRepository appointmentRepository;

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

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        // Проверка пересечения: ищем слоты того же дня недели
        List<WorkSlot> existing = workSlotRepository.findByMasterIdAndDayOfWeek(masterId, slot.getDayOfWeek());
        for (WorkSlot s : existing) {
            boolean overlap = !(slot.getEndTime().isBefore(s.getStartTime()) || slot.getStartTime().isAfter(s.getEndTime()));
            if (overlap) {
                log.warn("New slot overlaps with existing slot id={} for master id={}", s.getId(), masterId);
                throw new IllegalArgumentException("New slot overlaps with existing slot id=" + s.getId());
            }
        }

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

        // Проверка пересечения с другими слотами мастера (исключая текущий) для указанного дня
        List<WorkSlot> others = workSlotRepository.findByMasterIdAndDayOfWeek(masterId, slot.getDayOfWeek());
        for (WorkSlot s : others) {
            if (s.getId().equals(slotId)) continue;
            boolean overlap = !(slot.getEndTime().isBefore(s.getStartTime()) || slot.getStartTime().isAfter(s.getEndTime()));
            if (overlap) {
                log.warn("Updated slot would overlap with existing slot id={} for master id={}", s.getId(), masterId);
                throw new IllegalArgumentException("Updated slot overlaps with existing slot id=" + s.getId());
            }
        }

        boolean hasAppointments = appointmentRepository.existsBySlot_Id(slotId);
        if (hasAppointments) {
            if (!slot.getStartTime().equals(existing.getStartTime()) || !slot.getEndTime().equals(existing.getEndTime())
                    || !slot.getDayOfWeek().equals(existing.getDayOfWeek())) {
                log.warn("Attempt to change times/day of slot id={} with existing appointments", slotId);
                throw new IllegalStateException("Cannot change slot time/day because there are existing appointments");
            }
        }

        existing.setDayOfWeek(slot.getDayOfWeek());
        existing.setStartTime(slot.getStartTime());
        existing.setEndTime(slot.getEndTime());
        existing.setNote(slot.getNote());

        WorkSlot saved = workSlotRepository.save(existing);
        log.info("Updated work slot id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long slotId) {
        Assert.notNull(slotId, "slotId must not be null");
        WorkSlot existing = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + slotId));

        boolean hasAppointments = appointmentRepository.existsBySlot_Id(slotId);
        if (hasAppointments) {
            log.warn("Attempt to delete slot id={} with existing appointments", slotId);
            throw new IllegalStateException("Cannot delete slot with existing appointments");
        }

        workSlotRepository.deleteById(slotId);
        log.info("Deleted work slot id={}", slotId);
    }

    @Override
    public List<WorkSlot> findByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId)) throw new EntityNotFoundException("Master not found id=" + masterId);
        return workSlotRepository.findByMasterId(masterId);
    }

    @Override
    public List<WorkSlot> findAvailable(Long masterId, LocalDate date) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(date, "date must not be null");
        if (!masterRepository.existsById(masterId)) throw new EntityNotFoundException("Master not found id=" + masterId);

        DayOfWeek dow = date.getDayOfWeek();
        String dayName = dow.name(); // MONDAY, TUESDAY...

        // Возвращаем все слоты для этого дня; фронт/клиент решит, какие из них свободны по времени
        return workSlotRepository.findByMasterIdAndDayOfWeek(masterId, dayName);
    }
}
