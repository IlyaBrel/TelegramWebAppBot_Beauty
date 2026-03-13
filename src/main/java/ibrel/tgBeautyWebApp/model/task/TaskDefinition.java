package ibrel.tgBeautyWebApp.model.task;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_definitions", indexes = {
        @Index(name = "idx_task_code",   columnList = "code", unique = true),
        @Index(name = "idx_task_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Уникальный код задания, например "SUBSCRIBE_TG", "INVITE_3_FRIENDS" */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TaskType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    /** Количество баллов за выполнение */
    @Column(name = "points_reward", nullable = false)
    @Builder.Default
    private Integer pointsReward = 0;

    /**
     * Пороговое значение для заданий типа "пригласи N друзей".
     * null — задание без порога (одноразовое).
     */
    @Column(name = "threshold_value")
    private Integer threshold;

    @Builder.Default
    private Boolean active = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    public enum TaskType {
        SUBSCRIBE_TG,
        SUBSCRIBE_INSTAGRAM,
        LEAVE_REVIEWS,
        INVITE_FRIENDS,
        FRIENDS_COMPLETE_ORDERS,
        CUSTOM
    }
}
