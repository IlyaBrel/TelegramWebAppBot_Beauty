package ibrel.tgBeautyWebApp.dto.catalog;

import ibrel.tgBeautyWebApp.model.Promotion.PromotionType;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDto {
    private Long          id;
    private String        title;
    private String        description;
    private String        imageUrl;
    private String        actionUrl;

    /**
     * Тип акции: INFO, PROMO, ANNOUNCEMENT.
     * Используем enum напрямую — Jackson сериализует/десериализует по имени.
     */
    private PromotionType type;

    private Integer       sortOrder;
    private Boolean       active;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
}