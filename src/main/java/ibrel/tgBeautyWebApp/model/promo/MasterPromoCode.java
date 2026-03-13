package ibrel.tgBeautyWebApp.model.promo;

import ibrel.tgBeautyWebApp.model.master.Master;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "master_promo_codes", indexes = {
        @Index(name = "idx_promo_code",      columnList = "code", unique = true),
        @Index(name = "idx_promo_master_id", columnList = "master_id"),
        @Index(name = "idx_promo_active",    columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterPromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromoType type;

    /** Значение скидки: процент или фиксированная сумма */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /** false когда usedCount >= usageLimit или истёк срок */
    @Builder.Default
    private Boolean active = true;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    public enum PromoType {
        PERCENT,
        FIXED_AMOUNT
    }
}
