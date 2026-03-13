package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface WorkSlotRepository extends JpaRepository<WorkSlot, Long> {

    List<WorkSlot> findByMaster_IdOrderByDayOfWeekAscStartTimeAsc(Long masterId);

    List<WorkSlot> findByMaster_IdAndDayOfWeek(Long masterId, String dayOfWeek);

    List<WorkSlot> findByMaster_IdAndDayOfWeekAndStartTimeBetween(
            Long masterId, String dayOfWeek, LocalTime from, LocalTime to);

    boolean existsByMaster_IdAndId(Long masterId, Long slotId);

    /** Свободные слоты мастера на конкретный день недели */
    @Query("select ws from WorkSlot ws " +
            "where ws.master.id = :masterId " +
            "and ws.dayOfWeek = :dayOfWeek " +
            "and not exists (" +
            "  select 1 from AppointmentSlot s " +
            "  where s.slot.id = ws.id " +
            "  and s.appointment.status not in ('CANCELLED', 'REJECTED')" +
            ") " +
            "order by ws.startTime asc")
    List<WorkSlot> findFreeSlots(
            @Param("masterId")  Long masterId,
            @Param("dayOfWeek") String dayOfWeek);

    /**
     * Свободные слоты начиная с заданного времени — для подбора нескольких
     * подряд идущих слотов при длинном заказе.
     */
    @Query("select ws from WorkSlot ws " +
            "where ws.master.id = :masterId " +
            "and ws.dayOfWeek = :dayOfWeek " +
            "and ws.startTime >= :from " +
            "and not exists (" +
            "  select 1 from AppointmentSlot s " +
            "  where s.slot.id = ws.id " +
            "  and s.appointment.status not in ('CANCELLED', 'REJECTED')" +
            ") " +
            "order by ws.startTime asc")
    List<WorkSlot> findFreeFrom(
            @Param("masterId")  Long masterId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("from")      LocalTime from);
}