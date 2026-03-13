package ibrel.tgBeautyWebApp.repository.settings;

import ibrel.tgBeautyWebApp.model.settings.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {
}
