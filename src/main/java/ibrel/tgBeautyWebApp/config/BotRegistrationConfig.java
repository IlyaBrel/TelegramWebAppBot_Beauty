package ibrel.tgBeautyWebApp.config;

import ibrel.tgBeautyWebApp.BotManagement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
public class BotRegistrationConfig {
    private final BotManagement botManagement;

    public BotRegistrationConfig(BotManagement botManagement) {
        this.botManagement = botManagement;
    }

    @Bean
    public TelegramBotsLongPollingApplication botsApplication() throws TelegramApiException {
        String botToken = "8404148484:AAG45HS0eqZt1CFKtya72r70ABq-P3bsLpA";
        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(botToken, botManagement);
        return botsApplication;
    }
}
