package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.master.WorkSlot;

import java.time.LocalDate;
import java.util.List;

public interface WorkSlotService {

    /**
     * Создать слот для мастера.
     */
    WorkSlot create(Long masterId, WorkSlot slot);

    /**
     * Обновить существующий слот.
     */
    WorkSlot update(Long slotId, WorkSlot slot);

    /**
     * Удалить слот (проверка на существующие записи).
     */
    void delete(Long slotId);

    /**
     * Получить все слоты мастера.
     */
    List<WorkSlot> findByMaster(Long masterId);

    /**
     * Получить доступные слоты мастера на конкретную дату.
     * Реализовано через dayOfWeek в WorkSlot (MONDAY..SUNDAY).
     */
    List<WorkSlot> findAvailable(Long masterId, LocalDate date);
}
