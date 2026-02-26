package ibrel.tgBeautyWebApp.service.impl;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserCommandHandler {
    private final UserService userService;
    private final TelegramClient telegramClient;

    public void handleStart(Update update) {
        User tgUser = update.getMessage().getFrom();
        Long telegramId = tgUser.getId();

        // сохраняем пользователя
        userService.findByTelegramId(telegramId)
                .orElseGet(() -> userService.save(
                        UserTG.builder()
                                .telegramId(telegramId)
                                .username(tgUser.getUserName())
                                .firstName(tgUser.getFirstName())
                                .lastName(tgUser.getLastName())
                                .languageCode(tgUser.getLanguageCode())
                                .role(UserRole.USER)
                                .active(true)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));

        // приветственное сообщение
        SendMessage msg = new SendMessage(
                String.valueOf(update.getMessage().getChatId()),
                "Привет, " + tgUser.toString() + "! Добро пожаловать в сервис записи."
        );

        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void handleUnknown(Update update) {
        SendMessage msg = new SendMessage(
                String.valueOf(update.getMessage().getChatId()),
                "Извини, я пока не знаю такой команды."
        );
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
