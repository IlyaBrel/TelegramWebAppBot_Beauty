package ibrel.tgBeautyWebApp.model.settings;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Глобальный кэшбэк (%) — используется если у мастера не задан свой */
    @Column(name = "cashback_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cashbackPercent = BigDecimal.ZERO;

    /** Курс конвертации: N баллов = 1 рубль */
    @Column(name = "points_to_ruble_rate", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pointsToRubleRate = BigDecimal.ONE;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** telegramId администратора, последним обновившего настройки */
    @Column(name = "updated_by")
    private Long updatedBy;
}
