package ibrel.tgBeautyWebApp.model.campaign;

import ibrel.tgBeautyWebApp.model.master.Master;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "campaigns", indexes = {
        @Index(name = "idx_campaign_type",   columnList = "type"),
        @Index(name = "idx_campaign_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignType type;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    private Boolean active = true;

    // ── MASTER_ACTION / MASTER_DISCOUNT — мастер-владелец акции ─────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    private Master master;

    // ── MASTER_ACTION — список действий ─────────────────────────────────────
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampaignAction> actions;

    // ── PROMO — скидки мастеров с процентами ────────────────────────────────
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampaignMasterDiscount> masterDiscounts;

    public enum CampaignType {
        PROMO,
        MASTER_ACTION,
        MASTER_DISCOUNT
    }
}
