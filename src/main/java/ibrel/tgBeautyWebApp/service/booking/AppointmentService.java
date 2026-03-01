package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;

import java.util.List;

public interface AppointmentService {

    Appointment createAppointment(String clientId,
                                  Long masterId,
                                  Long slotId,
                                  List<ServiceSelectionDto> services);

    Appointment getById(Long id);

    List<Appointment> getByMaster(Long masterId);

    List<Appointment> getByClient(String clientId);

    Appointment cancel(Long appointmentId, String byClientId);

    Appointment confirm(Long appointmentId);
}
