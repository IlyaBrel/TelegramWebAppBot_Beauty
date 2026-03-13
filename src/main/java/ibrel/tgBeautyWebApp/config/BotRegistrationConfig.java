package ibrel.tgBeautyWebApp.config;

import ibrel.tgBeautyWebApp.BotManagement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
public class BotRegistrationConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final BotManagement botManagement;

    public BotRegistrationConfig(BotManagement botManagement) {
        this.botManagement = botManagement;
    }

    @Bean
    public TelegramBotsLongPollingApplication botsApplication() throws TelegramApiException {
        TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
        app.registerBot(botToken, botManagement);
        return app;
    }
}