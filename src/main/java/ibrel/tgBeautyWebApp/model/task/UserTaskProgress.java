package ibrel.tgBeautyWebApp.model.task;

import ibrel.tgBeautyWebApp.model.UserTG;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_task_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "task_definition_id"}),
       indexes = {
               @Index(name = "idx_utp_user_id", columnList = "user_id"),
               @Index(name = "idx_utp_completed", columnList = "completed")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTaskProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserTG user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_definition_id", nullable = false)
    private TaskDefinition taskDefinition;

    @Builder.Default
    private Boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Текущий прогресс для заданий с порогом (например, 2 из 5 друзей) */
    @Column(name = "current_progress", nullable = false)
    @Builder.Default
    private Integer currentProgress = 0;
}
