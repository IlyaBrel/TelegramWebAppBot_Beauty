package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.booking.AppointmentServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentServiceItemRepository extends JpaRepository<AppointmentServiceItem, Long> {
}
