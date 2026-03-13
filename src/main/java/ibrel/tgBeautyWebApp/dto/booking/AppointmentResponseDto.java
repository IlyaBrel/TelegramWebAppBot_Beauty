package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDto {

    private Long id;

    /** TelegramId клиента */
    private Long clientTelegramId;

    private Long masterId;
    private String masterName;

    /** Все слоты записи, упорядоченные по slotOrder */
    private List<WorkSlotShortDto> slots;

    private String status;
    private String rejectionReason;

    private List<AppointmentItemDto> items;

    private Integer totalDurationMinutes;
    private Double  totalPrice;

    /** Код применённого промокода (null если не использовался) */
    private String promoCode;

    /** Сумма скидки по промокоду */
    private BigDecimal promoDiscountAmount;

    /** Списанные бонусы */
    private BigDecimal bonusAmountUsed;

    /** Начисленный кэшбэк после завершения */
    private BigDecimal cashbackAmount;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
