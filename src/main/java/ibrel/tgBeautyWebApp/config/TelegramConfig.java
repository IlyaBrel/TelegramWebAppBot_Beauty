package ibrel.tgBeautyWebApp.config;

import ibrel.tgBeautyWebApp.BotManagement;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient("8404148484:AAG45HS0eqZt1CFKtya72r70ABq-P3bsLpA");
    }
}
