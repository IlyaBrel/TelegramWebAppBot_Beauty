package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.*;

/**
 * Удобство / доп. сервис мастера (туалет, гардероб, парковка, чистота и т.д.)
 * Создаётся ТОЛЬКО администратором.
 * Привязывается к мастеру при его создании/редактировании (также только admin).
 * НЕ входит в состав заказа — только информационный блок.
 */
@Entity
@Table(name = "amenities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** Иконка для UI (emoji или название иконки) */
    @Column(length = 50)
    private String icon;

    @Builder.Default
    private Boolean active = true;
}