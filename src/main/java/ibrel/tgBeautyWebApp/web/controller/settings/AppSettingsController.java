package ibrel.tgBeautyWebApp.web.controller.settings;

import ibrel.tgBeautyWebApp.model.settings.AppSettings;
import ibrel.tgBeautyWebApp.service.settings.AppSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class AppSettingsController {

    private final AppSettingsService appSettingsService;

    /** Получить текущие настройки (публичный — кэшбэк и курс конвертации нужны клиенту) */
    @GetMapping
    public ResponseEntity<AppSettings> get() {
        return ResponseEntity.ok(appSettingsService.get());
    }

    /**
     * Обновить глобальные настройки (только администратор).
     * adminTelegramId логируется как updatedBy.
     */
    @PutMapping
    public ResponseEntity<AppSettings> update(
            @RequestParam BigDecimal cashbackPercent,
            @RequestParam BigDecimal pointsToRubleRate,
            @RequestParam Long adminTelegramId) {
        AppSettings updated = appSettingsService.save(cashbackPercent, pointsToRubleRate, adminTelegramId);
        return ResponseEntity.ok(updated);
    }
}
