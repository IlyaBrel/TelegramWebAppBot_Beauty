package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.*;

/**
 * Специализация мастера (Маникюр, Брови, Ресницы, Стрижка и т.д.)
 * Создаётся только администратором.
 * Мастер может иметь несколько специализаций.
 */
@Entity
@Table(name = "specialties", indexes = {
        @Index(name = "idx_specialty_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** Иконка / emoji для отображения в UI */
    @Column(length = 50)
    private String icon;

    @Builder.Default
    private Boolean active = true;
}