package ibrel.tgBeautyWebApp.model.promo;

import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "master_service_discounts", indexes = {
        @Index(name = "idx_msd_master_id",  columnList = "master_id"),
        @Index(name = "idx_msd_service_id", columnList = "service_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterServiceDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MasterServiceWork service;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "discount_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountPrice;

    /** Вычисляется динамически: (basePrice - discountPrice) / basePrice * 100 */
    @Transient
    public BigDecimal getDiscountPercent() {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return basePrice.subtract(discountPrice)
                .divide(basePrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
