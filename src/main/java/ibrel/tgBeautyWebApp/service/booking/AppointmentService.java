package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;

import java.util.List;

public interface AppointmentService {

    /** Создание заказа. Автоматически занимает нужное количество слотов. */
    Appointment createAppointment(Long clientTelegramId,
                                  Long masterId,
                                  Long startSlotId,
                                  List<ServiceSelectionDto> services,
                                  Long certificateId);

    Appointment getById(Long id);

    List<Appointment> getByMaster(Long masterId);

    List<Appointment> getByClient(Long clientTelegramId);

    // getByClientAndStatus() удалён — нигде не вызывался ни из одного контроллера.
    // При необходимости фильтровать по статусу — добавить endpoint в AppointmentController.

    /** Отмена заказа пользователем */
    Appointment cancelByClient(Long appointmentId, Long clientTelegramId);

    /** Подтверждение мастером (через Telegram callback) */
    Appointment confirmByMaster(Long appointmentId, Long masterTelegramId);

    /** Отклонение мастером с причиной */
    Appointment rejectByMaster(Long appointmentId, Long masterTelegramId, String reason);

    /** Завершение заказа (вызывается мастером или планировщиком) */
    Appointment complete(Long appointmentId, Long masterTelegramId);

    /** Отправка запроса на отзыв после завершения */
    void sendReviewRequest(Long appointmentId);
}