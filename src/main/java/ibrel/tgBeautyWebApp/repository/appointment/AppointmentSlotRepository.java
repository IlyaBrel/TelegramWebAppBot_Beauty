package ibrel.tgBeautyWebApp.repository.appointment;

import ibrel.tgBeautyWebApp.model.booking.AppointmentSlot;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    /**
     * Проверяет, есть ли у слота активные записи.
     * "Активные" = статус не CANCELLED и не REJECTED.
     *
     * Используется в:
     *   - WorkSlotServiceImpl.update() — нельзя менять время если слот занят
     *   - WorkSlotServiceImpl.delete() — нельзя удалить занятый слот
     *   - AppointmentServiceImpl.createAppointment() — проверка перед бронированием
     *   - AppointmentCalcServiceImpl.checkAvailability() и findNearestSlots()
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM AppointmentSlot s " +
            "WHERE s.slot.id = :slotId " +
            "AND s.appointment.status NOT IN " +
            "(:cancelledStatus, :rejectedStatus)")
    boolean existsBySlotIdAndActive(
            @Param("slotId")          Long slotId,
            @Param("cancelledStatus") Appointment.Status cancelledStatus,
            @Param("rejectedStatus")  Appointment.Status rejectedStatus
    );
}