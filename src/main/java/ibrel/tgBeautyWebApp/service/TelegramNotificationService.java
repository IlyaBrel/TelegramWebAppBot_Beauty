package ibrel.tgBeautyWebApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService {

    private final TelegramClient telegramClient;

    public void sendMessage(Long telegramId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(telegramId), text);
        try {
            telegramClient.execute(msg);
            log.info("Sent message to telegramId={}", telegramId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to telegramId={}: {}", telegramId, e.getMessage());
            throw new RuntimeException("Failed to send Telegram message: " + e.getMessage());
        }
    }

    // Уведомление при активации
    public void notifyActivated(Long telegramId) {
        sendMessage(telegramId, "✅ Ваш аккаунт активирован! Теперь вы можете пользоваться сервисом.");
    }

    // Уведомление о новой записи
    public void notifyAppointmentCreated(Long telegramId, String masterName, String date) {
        sendMessage(telegramId, String.format(
                "📅 Ваша запись подтверждена!\nМастер: %s\nВремя: %s", masterName, date));
    }

    // Уведомление об отмене
    public void notifyAppointmentCancelled(Long telegramId) {
        sendMessage(telegramId, "❌ Ваша запись была отменена.");
    }
}