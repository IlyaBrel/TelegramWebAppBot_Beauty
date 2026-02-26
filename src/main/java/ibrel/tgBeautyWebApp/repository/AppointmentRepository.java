package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByMasterId(Long masterId);
    List<Appointment> findByUserId(Long userId);
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<String> statuses);
    List<Appointment> findBySlotIdAndStatusIn(Long slotId, Collection<String> statuses);
}
