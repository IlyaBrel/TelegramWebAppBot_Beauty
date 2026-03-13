package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreateRequestDto {

    /** TelegramId клиента (не String) */
    @NotNull(message = "clientTelegramId is required")
    private Long clientTelegramId;

    @NotNull(message = "masterId is required")
    private Long masterId;

    /** Первый слот — сервис сам подберёт последующие если нужно */
    @NotNull(message = "startSlotId is required")
    private Long startSlotId;

    @NotEmpty(message = "services must not be empty")
    private List<ServiceSelectionDto> services;

    /** Код сертификата (опционально — если пользователь оплачивает сертификатом) */
    private Long certificateCode;
}
