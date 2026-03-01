package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsBySlot_Id(Long slotId);

    List<Appointment> findByMaster_IdOrderByCreatedAtDesc(Long masterId);

    List<Appointment> findByClientIdOrderByCreatedAtDesc(String clientId);

    // Быстрая проверка: существует ли appointment, где в items есть service с заданным id
    @Query("select case when count(a)>0 then true else false end " +
            "from Appointment a join a.items i where i.service.id = :serviceId")
    boolean existsByItems_Service_Id(@Param("serviceId") Long serviceId);

    // Быстрая проверка: существует ли appointment, где в items.variableDetails есть variableDetail с заданным id
    @Query("select case when count(a)>0 then true else false end " +
            "from Appointment a join a.items i join i.variableDetails v where v.id = :variableDetailId")
    boolean existsByItems_VariableDetails_Id(@Param("variableDetailId") Long variableDetailId);

    // Альтернатива (нативный SQL) — полезно при сложных схемах/производительности
    // @Query(value = "select exists (select 1 from appointments a " +
    //        "join appointment_service_items i on i.appointment_id = a.id " +
    //        "where i.service_id = :serviceId)", nativeQuery = true)
    // boolean existsServiceInAppointments(@Param("serviceId") Long serviceId);
}
