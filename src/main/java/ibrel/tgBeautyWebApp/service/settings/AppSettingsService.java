package ibrel.tgBeautyWebApp.service.settings;

import ibrel.tgBeautyWebApp.model.settings.AppSettings;

import java.math.BigDecimal;

public interface AppSettingsService {

    AppSettings get();

    AppSettings save(BigDecimal cashbackPercent, BigDecimal pointsToRubleRate, Long adminTelegramId);
}
