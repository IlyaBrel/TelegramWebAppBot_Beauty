package ibrel.tgBeautyWebApp.model.campaign;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaign_actions", indexes = {
        @Index(name = "idx_ca_campaign_id", columnList = "campaign_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActionType type;

    @Column(length = 1000)
    private String description;

    public enum ActionType {
        POST_STORY,
        SUBSCRIBE_TG,
        OTHER
    }
}
