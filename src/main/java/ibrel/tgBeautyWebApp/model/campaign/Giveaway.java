package ibrel.tgBeautyWebApp.model.campaign;

import ibrel.tgBeautyWebApp.model.UserTG;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "giveaways", indexes = {
        @Index(name = "idx_giveaway_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Giveaway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Если true — участие требует подтверждения от администратора */
    @Column(name = "requires_confirmation", nullable = false)
    @Builder.Default
    private Boolean requiresConfirmation = false;

    @Builder.Default
    private Boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "giveaway_participants",
            joinColumns        = @JoinColumn(name = "giveaway_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserTG> participants;
}
