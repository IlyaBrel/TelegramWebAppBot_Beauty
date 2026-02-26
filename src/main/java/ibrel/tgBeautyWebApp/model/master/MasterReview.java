package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "master_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorName;
    private String photoURL;
    private Integer rating;
    private String comment;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "master_id")
    private Master master;
}

