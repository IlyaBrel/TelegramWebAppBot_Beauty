package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreateRequestDto {

    /** TelegramId клиента */
    @NotNull(message = "clientTelegramId is required")
    private Long clientTelegramId;

    @NotNull(message = "masterId is required")
    private Long masterId;

    /** Первый слот — сервис сам подберёт последующие если нужно */
    @NotNull(message = "startSlotId is required")
    private Long startSlotId;

    @NotEmpty(message = "services must not be empty")
    private List<ServiceSelectionDto> services;

    /**
     * Код промокода (опционально).
     * XOR с bonusAmountToUse — нельзя использовать оба одновременно.
     */
    private String promoCode;

    /**
     * Сумма для списания с бонусного счёта (опционально).
     * XOR с promoCode — нельзя использовать оба одновременно.
     */
    private BigDecimal bonusAmountToUse;
}
