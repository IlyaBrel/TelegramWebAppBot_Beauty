package ibrel.tgBeautyWebApp;

import ibrel.tgBeautyWebApp.service.impl.UserCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class BotManagement implements LongPollingSingleThreadUpdateConsumer {

    private final UserCommandHandler userCommandHandler;


    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();

            if (text.equals("/start")) {
                userCommandHandler.handleStart(update);
            } else {
                userCommandHandler.handleUnknown(update);
            }
        }
    }
}
