package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;

@Entity
@Table(name = "master_work_examples", indexes = {
        @Index(name = "idx_work_example_master_id", columnList = "master_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterWorkExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    /**
     * Ссылка на изображение/файл (URL в S3, CDN или относительный путь).
     * Для больших файлов хранение в БД не рекомендуется.
     */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    @JsonIgnore
    private Master master;
}
