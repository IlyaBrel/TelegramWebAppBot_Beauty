package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentRequestDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;

import java.util.Collections;
import java.util.List;

public interface AppointmentService {


    Appointment create(AppointmentRequestDto request);

    Appointment getById(Long id);

    List<Appointment> getByMaster(Long masterId);

    List<Appointment> getByUser(Long userId);

    /**
     * Отменить запись. Проверяет права/статус и переводит в статус CANCELLED.
     *
     * @param appointmentId     id записи
     * @param requestedByUserId id пользователя, который запрашивает отмену (для проверки прав)
     * @return обновлённая запись
     */
    Appointment cancel(Long appointmentId, Long requestedByUserId);

    /**
     * Пометить запись как выполненную.
     *
     * @param appointmentId id записи
     * @return обновлённая запись
     */
    Appointment complete(Long appointmentId);

    /**
     * Создать запись.
     *
     * @param userId     id клиента
     * @param masterId   id мастера
     * @param slotId     id слота
     * @param serviceIds список id услуг
     * @return созданная Appointment
     */
    // backward-compatible adapter
    default Appointment create(Long userId, Long masterId, Long slotId, List<Long> serviceIds) {
        AppointmentRequest req =
                AppointmentRequest
                        .builder()
                        .userId(userId)
                        .masterId(masterId)
                        .slotId(slotId)
                        .serviceIds(serviceIds)
                        .variableSelections(Collections.emptyList()).build();
        return create(req);
    }
}
