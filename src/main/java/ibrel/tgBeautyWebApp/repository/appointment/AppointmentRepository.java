package ibrel.tgBeautyWebApp.repository.appointment;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByMaster_IdOrderByCreatedAtDesc(Long masterId);

    List<Appointment> findByClientTelegramIdOrderByCreatedAtDesc(Long clientTelegramId);

    List<Appointment> findByClientTelegramIdAndStatusOrderByCreatedAtDesc(
            Long clientTelegramId, Appointment.Status status);

    List<Appointment> findByMaster_IdAndStatusOrderByCreatedAtDesc(
            Long masterId, Appointment.Status status);

    /** Проверка: занят ли слот активным заказом */
    @Query("select case when count(s) > 0 then true else false end " +
            "from AppointmentSlot s " +
            "where s.slot.id = :slotId " +
            "and s.appointment.status not in ('CANCELLED', 'REJECTED')")
    boolean isSlotBooked(@Param("slotId") Long slotId);

    /** Есть ли заказы с данной услугой */
    @Query("select case when count(a) > 0 then true else false end " +
            "from Appointment a join a.items i where i.service.id = :serviceId")
    boolean existsByItems_Service_Id(@Param("serviceId") Long serviceId);

    /** Есть ли заказы с данным вариантом услуги */
    @Query("select case when count(a) > 0 then true else false end " +
            "from Appointment a join a.items i join i.variableDetails v where v.id = :variableDetailId")
    boolean existsByItems_VariableDetails_Id(@Param("variableDetailId") Long variableDetailId);

    /** Подтверждённые заказы без напоминания — для планировщика */
    @Query("select distinct a from Appointment a join a.slots s " +
            "where a.status = 'CONFIRMED' " +
            "and a.reminderSent = false " +
            "and s.slotOrder = 0")
    List<Appointment> findConfirmedWithoutReminder();

    /** Завершённые заказы без запроса на отзыв — для планировщика */
    List<Appointment> findByStatusAndReviewRequestSentFalse(Appointment.Status status);

    /** Статистика по статусам для мастера */
    @Query("select a.status, count(a) from Appointment a " +
            "where a.master.id = :masterId group by a.status")
    List<Object[]> countByStatusForMaster(@Param("masterId") Long masterId);
}