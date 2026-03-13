package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;

import java.math.BigDecimal;
import java.util.List;

public interface AppointmentService {

    /**
     * Создание заказа. Автоматически занимает нужное количество слотов.
     * XOR-правило: нельзя одновременно указать promoCode и bonusAmountToUse.
     */
    Appointment createAppointment(Long clientTelegramId,
                                  Long masterId,
                                  Long startSlotId,
                                  List<ServiceSelectionDto> services,
                                  String promoCode,
                                  BigDecimal bonusAmountToUse);

    Appointment getById(Long id);

    List<Appointment> getByMaster(Long masterId);

    List<Appointment> getByClient(Long clientTelegramId);

    /** Отмена заказа пользователем */
    Appointment cancelByClient(Long appointmentId, Long clientTelegramId);

    /** Подтверждение мастером (через Telegram callback) */
    Appointment confirmByMaster(Long appointmentId, Long masterTelegramId);

    /** Отклонение мастером с причиной */
    Appointment rejectByMaster(Long appointmentId, Long masterTelegramId, String reason);

    /**
     * Завершение заказа (вызывается мастером или планировщиком).
     * Автоматически начисляет кэшбэк и обновляет реферальные счётчики.
     */
    Appointment complete(Long appointmentId, Long masterTelegramId);

    /** Отправка запроса на отзыв после завершения */
    void sendReviewRequest(Long appointmentId);
}
