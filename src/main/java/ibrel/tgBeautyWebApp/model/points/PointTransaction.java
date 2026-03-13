package ibrel.tgBeautyWebApp.model.points;

import ibrel.tgBeautyWebApp.model.UserTG;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions", indexes = {
        @Index(name = "idx_ptx_user_id",    columnList = "user_id"),
        @Index(name = "idx_ptx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserTG user;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointReason reason;

    /** Код задания, если начисление за задание */
    @Column(name = "task_type", length = 100)
    private String taskType;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PointReason {
        TASK_COMPLETED,
        REFERRAL_BONUS,
        ADMIN_GRANT
    }
}
