package ibrel.tgBeautyWebApp.model.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;

@Entity
@Table(name = "master_reviews", indexes = {
        @Index(name = "idx_master_reviews_master_id", columnList = "master_id"),
        @Index(name = "idx_master_reviews_author_id", columnList = "author_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Оценка 1..5
     */
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    /**
     * Идентификатор автора отзыва (может быть userId из системы или внешний id)
     */
    @Column(name = "author_id", length = 200)
    private String authorId;

    private OffsetDateTime createdAt;

    /**
     * Связь с мастером
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    @JsonIgnore
    private Master master;

    /**
     * Опциональная связь с записью (если отзыв привязан к конкретной записи)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    @JsonIgnore
    private Appointment appointment;
}
