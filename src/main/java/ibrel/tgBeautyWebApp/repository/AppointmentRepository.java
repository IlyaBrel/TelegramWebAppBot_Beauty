package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByMasterId(Long masterId);

    List<Appointment> findByUserId(Long userId);

    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<String> statuses);

    List<Appointment> findBySlotIdAndStatusIn(Long slotId, Collection<String> statuses);


    boolean existsBySlot_Id(Long slotId);

    // Быстрая проверка: есть ли записи, которые используют конкретный сервис (если нужно)
    boolean existsByServices_Id(Long serviceId);

    // Проверка использования variable detail (если в модели Appointment есть связь)
    @Query("select case when count(a)>0 then true else false end from Appointment a join a.services s join s.variableDetails v where v.id = :variableId")
    boolean existsByVariableDetailsId(@Param("variableId") Long variableId);

}
