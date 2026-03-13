package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Город. Создаётся только администратором.
 * Мастер привязывается к городу через MasterAddress.
 */
@Entity
@Table(name = "cities", indexes = {
        @Index(name = "idx_city_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Регион / область (необязательно) */
    @Column(length = 100)
    private String region;

    /** Флаг активности: можно скрыть город без удаления */
    @Builder.Default
    private Boolean active = true;
}