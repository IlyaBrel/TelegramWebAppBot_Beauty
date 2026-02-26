package ibrel.tgBeautyWebApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
public class TgBeautyWebAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TgBeautyWebAppApplication.class, args);
    }
}
