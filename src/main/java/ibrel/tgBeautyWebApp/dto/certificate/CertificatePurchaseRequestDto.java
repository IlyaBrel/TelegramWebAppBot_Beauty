package ibrel.tgBeautyWebApp.dto.certificate;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificatePurchaseRequestDto {
    private Long   id;

    /** ID оффера — обязателен при создании заявки */
    @NotNull(message = "offerId is required")
    private Long   offerId;
    private String offerTitle;
    private Double offerPrice;

    private Long   buyerTelegramId;

    /** Статус: PENDING, APPROVED, REJECTED, CANCELLED */
    private String status;

    @Size(max = 500, message = "comment length must be <= 500")
    private String buyerComment;

    private String rejectionReason;

    /** Выпущенный сертификат (заполняется после APPROVED) */
    private String certificateCode;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
