// файл: ibrel/tgBeautyWebApp/service/impl/UserCommandHandler.java
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserCommandHandler {

    private final UserService userService;
    private final TelegramClient telegramClient;

    public void handleStart(Update update) {
        User tgUser = update.getMessage().getFrom();
        Long telegramId = tgUser.getId();

        boolean hasAdmin = userService.hasAnyAdmin();

        userService.findByTelegramId(telegramId).ifPresentOrElse(existing -> {
            LocalDateTime last = existing.getUpdatedAt();
            // Если уже обновляли сегодня — не трогаем БД, только отправляем приветствие
            if (last != null && last.toLocalDate().isEqual(LocalDate.now())) {
                sendMessage(update, "Рады снова видеть вас 1, " + (existing.getFirstName() != null ? existing.getFirstName() : "пользователь") + "!");
                return;
            }

            boolean changed = false;
            if (!Objects.equals(existing.getUsername(), tgUser.getUserName())) {
                existing.setUsername(tgUser.getUserName()); changed = true;
            }
            if (!Objects.equals(existing.getFirstName(), tgUser.getFirstName())) {
                existing.setFirstName(tgUser.getFirstName()); changed = true;
            }
            if (!Objects.equals(existing.getLastName(), tgUser.getLastName())) {
                existing.setLastName(tgUser.getLastName()); changed = true;
            }
            if (!Objects.equals(existing.getLanguageCode(), tgUser.getLanguageCode())) {
                existing.setLanguageCode(tgUser.getLanguageCode()); changed = true;
            }

            // Обновляем метаданные
            existing.setUpdatedAt(LocalDateTime.now());

            // Сохраняем только если есть изменения полей или если нужно зафиксировать lastProfileUpdateAt
            // Для минимизации записей можно сохранять только при changed == true.
            if (changed) {
                userService.save(existing);
            } else {
                // Если хотите фиксировать факт "проверки" — раскомментируйте:
                userService.save(existing);
            }

            sendMessage(update, "Рады снова видеть вас, " + (existing.getFirstName() != null ? existing.getFirstName() : "пользователь") + "!");
        }, () -> {
            // Новый пользователь
            if (!hasAdmin) {
                UserTG admin = UserTG.builder()
                        .telegramId(telegramId)
                        .username(tgUser.getUserName())
                        .firstName(tgUser.getFirstName())
                        .lastName(tgUser.getLastName())
                        .languageCode(tgUser.getLanguageCode())
                        .role(UserRole.ADMIN)
                        .active(true)
                        .isInitialAdmin(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                userService.save(admin);
                sendMessage(update, "Вы зарегистрированы как первый администратор системы. Спасибо!");
            } else {
                UserTG user = UserTG.builder()
                        .telegramId(telegramId)
                        .username(tgUser.getUserName())
                        .firstName(tgUser.getFirstName())
                        .lastName(tgUser.getLastName())
                        .languageCode(tgUser.getLanguageCode())
                        .role(UserRole.USER)
                        .active(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                userService.save(user);
                sendMessage(update, "Спасибо за регистрацию. Ваш аккаунт ожидает подтверждения администратором. Вы получите уведомление, когда аккаунт будет активирован.");
            }
        });
    }

    private void sendMessage(Update update, String text) {
        SendMessage msg = new SendMessage(String.valueOf(update.getMessage().getChatId()), text);
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            // логирование ошибки отправки сообщения
            e.printStackTrace();
        }
    }

    public void handleUnknown(Update update) {
        SendMessage msg = new SendMessage(String.valueOf(update.getMessage().getChatId()),
                "Извини, я пока не знаю такой команды.");
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
