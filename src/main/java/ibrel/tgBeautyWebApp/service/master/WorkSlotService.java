package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.master.WorkSlot;

import java.util.List;

public interface WorkSlotService {

    WorkSlot create(Long masterId, WorkSlot slot);

    WorkSlot update(Long slotId, WorkSlot slot);

    void delete(Long slotId);

    List<WorkSlot> findByMaster(Long masterId);

    /**
     * Только свободные слоты (не занятые активными заказами).
     * dayOfWeek — строка MONDAY / TUESDAY / ... / SUNDAY.
     *
     * findAvailable(Long, LocalDate) удалён — нигде не вызывался.
     * MasterService экспонирует этот метод как getFreeSlots(Long, String).
     */
    List<WorkSlot> findFree(Long masterId, String dayOfWeek);
}