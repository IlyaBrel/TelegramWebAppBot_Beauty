package ibrel.tgBeautyWebApp.service.settings.impl;

import ibrel.tgBeautyWebApp.model.settings.AppSettings;
import ibrel.tgBeautyWebApp.repository.settings.AppSettingsRepository;
import ibrel.tgBeautyWebApp.service.settings.AppSettingsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppSettingsServiceImpl implements AppSettingsService {

    private final AppSettingsRepository repo;

    @Override
    public AppSettings get() {
        return repo.findAll().stream().findFirst()
                .orElseGet(() -> repo.save(AppSettings.builder()
                        .cashbackPercent(BigDecimal.valueOf(5))
                        .pointsToRubleRate(BigDecimal.valueOf(10))
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }

    @Override
    @Transactional
    public AppSettings save(BigDecimal cashbackPercent, BigDecimal pointsToRubleRate,
                            Long adminTelegramId) {
        AppSettings s = get();
        s.setCashbackPercent(cashbackPercent);
        s.setPointsToRubleRate(pointsToRubleRate);
        s.setUpdatedAt(LocalDateTime.now());
        s.setUpdatedBy(adminTelegramId);
        return repo.save(s);
    }
}
