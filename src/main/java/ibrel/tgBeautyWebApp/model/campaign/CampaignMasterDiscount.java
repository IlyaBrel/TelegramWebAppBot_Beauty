package ibrel.tgBeautyWebApp.model.campaign;

import ibrel.tgBeautyWebApp.model.master.Master;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Участник промо-кампании типа PROMO.
 * Хранит мастера и его процент скидки в рамках кампании.
 */
@Entity
@Table(name = "campaign_master_discounts", indexes = {
        @Index(name = "idx_cmd_campaign_id", columnList = "campaign_id"),
        @Index(name = "idx_cmd_master_id",   columnList = "master_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignMasterDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;
}
