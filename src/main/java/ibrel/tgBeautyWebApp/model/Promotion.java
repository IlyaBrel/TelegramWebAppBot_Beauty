package ibrel.tgBeautyWebApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Акция / информационный блок на главной странице.
 * Создаётся и управляется только администратором.
 */
@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_promo_active",     columnList = "active"),
        @Index(name = "idx_promo_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    /** Ссылка на акцию (URL или deep link в TG) */
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PromotionType type = PromotionType.INFO;

    /** Порядок отображения на главной (меньше = выше) */
    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean active = true;

    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;

    public enum PromotionType {
        INFO,       // информационный блок
        PROMO,      // акция со скидкой
        ANNOUNCEMENT // анонс нового мастера / услуги
    }
}