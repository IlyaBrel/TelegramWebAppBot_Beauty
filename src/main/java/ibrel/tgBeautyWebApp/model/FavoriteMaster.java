package ibrel.tgBeautyWebApp.model;

import ibrel.tgBeautyWebApp.model.master.Master;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Избранный мастер пользователя (звёздочка).
 * Уникальность: один пользователь — один раз добавляет мастера в избранное.
 */
@Entity
@Table(name = "favorite_masters", indexes = {
        @Index(name = "idx_favorite_user_id",   columnList = "user_telegram_id"),
        @Index(name = "idx_favorite_master_id",  columnList = "master_id")
},
        uniqueConstraints = @UniqueConstraint(
                name  = "uq_favorite_user_master",
                columnNames = {"user_telegram_id", "master_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_telegram_id", nullable = false)
    private Long userTelegramId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    private OffsetDateTime createdAt;
}