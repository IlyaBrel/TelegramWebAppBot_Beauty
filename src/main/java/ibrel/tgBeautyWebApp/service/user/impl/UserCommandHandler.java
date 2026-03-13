package ibrel.tgBeautyWebApp.service.user.impl;

import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import ibrel.tgBeautyWebApp.service.certificate.CertificatePurchaseService;
import ibrel.tgBeautyWebApp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCommandHandler {

    private final UserService                userService;
    private final AppointmentService         appointmentService;
    private final CertificatePurchaseService certificatePurchaseService;
    private final TelegramClient             telegramClient;

    /** URL фронтенда WebApp — берётся из application.properties */
    @Value("${webapp.url}")
    private String webAppUrl;

    // ── /start ────────────────────────────────────────────────────────────────

    public void handleStart(Update update) {
        User tgUser     = update.getMessage().getFrom();
        Long telegramId = tgUser.getId();
        boolean hasAdmin = userService.hasAnyAdmin();

        userService.findByTelegramId(telegramId).ifPresentOrElse(existing -> {
            // Обновляем данные если изменились
            boolean changed = false;
            if (!Objects.equals(existing.getUsername(),     tgUser.getUserName()))     { existing.setUsername(tgUser.getUserName());         changed = true; }
            if (!Objects.equals(existing.getFirstName(),    tgUser.getFirstName()))    { existing.setFirstName(tgUser.getFirstName());        changed = true; }
            if (!Objects.equals(existing.getLastName(),     tgUser.getLastName()))     { existing.setLastName(tgUser.getLastName());          changed = true; }
            if (!Objects.equals(existing.getLanguageCode(), tgUser.getLanguageCode())) { existing.setLanguageCode(tgUser.getLanguageCode());  changed = true; }
            if (changed) { existing.setUpdatedAt(LocalDateTime.now()); userService.save(existing); }

            String name = existing.getFirstName() != null ? existing.getFirstName() : "пользователь";

            if (!Boolean.TRUE.equals(existing.getActive())) {
                sendText(telegramId,
                        "⏳ Ваш аккаунт ещё не активирован.\n" +
                                "Ожидайте подтверждения администратора.");
            } else {
                sendWebAppWelcome(telegramId, name);
            }

        }, () -> {
            if (!hasAdmin) {
                // Первый пользователь — становится администратором
                UserTG admin = UserTG.builder()
                        .telegramId(telegramId).username(tgUser.getUserName())
                        .firstName(tgUser.getFirstName()).lastName(tgUser.getLastName())
                        .languageCode(tgUser.getLanguageCode()).role(UserRole.ADMIN)
                        .active(true).isInitialAdmin(true)
                        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
                userService.save(admin);
                sendWebAppWelcome(telegramId, tgUser.getFirstName() != null ? tgUser.getFirstName() : "Администратор");
            } else {
                // Обычный новый пользователь
                UserTG user = UserTG.builder()
                        .telegramId(telegramId).username(tgUser.getUserName())
                        .firstName(tgUser.getFirstName()).lastName(tgUser.getLastName())
                        .languageCode(tgUser.getLanguageCode()).role(UserRole.USER)
                        .active(false)
                        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
                userService.save(user);
                sendText(telegramId,
                        "👋 Добро пожаловать!\n\n" +
                                "⏳ Ваш аккаунт ожидает подтверждения администратора.\n" +
                                "Как только вас активируют — вы получите уведомление.");
            }
        });
    }

    /**
     * Приветственное сообщение с кнопкой открытия WebApp.
     * Кнопка типа web_app — открывает мини-приложение прямо в Telegram.
     */
    private void sendWebAppWelcome(Long telegramId, String name) {
        String text = String.format(
                "👋 Привет, <b>%s</b>!\n\n" +
                        "💅 Добро пожаловать в Beauty App — сервис записи к мастерам красоты.\n\n" +
                        "Нажмите кнопку ниже чтобы открыть каталог мастеров и записаться:",
                name);

        // Кнопка открытия WebApp — работает только по HTTPS URL
        InlineKeyboardButton webAppBtn = InlineKeyboardButton.builder()
                .text("💅 Открыть Beauty App")
                .webApp(WebAppInfo.builder().url(webAppUrl).build())
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(webAppBtn)))
                .build();

        SendMessage msg = SendMessage.builder()
                .chatId(telegramId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to send WebApp welcome to {}: {}", telegramId, e.getMessage());
        }
    }

    public void handleUnknown(Update update) {
        sendText(update.getMessage().getChatId(),
                "❓ Неизвестная команда.\n\nНапишите /start чтобы открыть приложение.");
    }

    // ── Callback-кнопки (мастер одобряет / отклоняет записи и сертификаты) ───

    public void handleCallback(Update update) {
        CallbackQuery callback        = update.getCallbackQuery();
        String        data            = callback.getData();
        Long          senderTelegramId = callback.getFrom().getId();

        try {
            if (data.startsWith("apt_confirm:")) {
                Long appointmentId = Long.parseLong(data.split(":")[1]);
                appointmentService.confirmByMaster(appointmentId, senderTelegramId);
                answerCallback(callback.getId(), "✅ Запись подтверждена");

            } else if (data.startsWith("apt_reject:")) {
                Long appointmentId = Long.parseLong(data.split(":")[1]);
                sendText(senderTelegramId,
                        "✍️ Напишите причину отклонения заявки #" + appointmentId + ":\n\n" +
                                "<code>reject_reason:" + appointmentId + " Причина</code>");
                answerCallback(callback.getId(), "Введите причину");

            } else if (data.startsWith("cert_approve:")) {
                Long requestId = Long.parseLong(data.split(":")[1]);
                certificatePurchaseService.approvePurchase(requestId, senderTelegramId);
                answerCallback(callback.getId(), "✅ Сертификат выдан покупателю");

            } else if (data.startsWith("cert_reject:")) {
                Long requestId = Long.parseLong(data.split(":")[1]);
                sendText(senderTelegramId,
                        "✍️ Напишите причину отклонения заявки на сертификат #" + requestId + ":\n\n" +
                                "<code>cert_reject_reason:" + requestId + " Причина</code>");
                answerCallback(callback.getId(), "Введите причину");

            } else if (data.startsWith("review:")) {
                Long appointmentId = Long.parseLong(data.split(":")[1]);
                sendText(senderTelegramId,
                        "⭐ Оставьте отзыв о записи #" + appointmentId + " через приложение.");
                answerCallback(callback.getId(), "Открываем форму");
            }

        } catch (Exception e) {
            log.error("Error handling callback data={}: {}", data, e.getMessage());
            answerCallback(callback.getId(), "Ошибка обработки");
        }
    }

    // ── Текстовые команды с payload (ответы мастера) ─────────────────────────

    public void handleMasterTextReply(Update update) {
        String text             = update.getMessage().getText();
        Long   senderTelegramId = update.getMessage().getFrom().getId();

        try {
            if (text.startsWith("reject_reason:")) {
                String[] parts = text.split(" ", 2);
                Long appointmentId = Long.parseLong(parts[0].split(":")[1]);
                String reason = parts.length > 1 ? parts[1] : "Причина не указана";
                appointmentService.rejectByMaster(appointmentId, senderTelegramId, reason);
                sendText(senderTelegramId, "✅ Заявка #" + appointmentId + " отклонена.");

            } else if (text.startsWith("cert_reject_reason:")) {
                String[] parts = text.split(" ", 2);
                Long requestId = Long.parseLong(parts[0].split(":")[1]);
                String reason  = parts.length > 1 ? parts[1] : "Причина не указана";
                certificatePurchaseService.rejectPurchase(requestId, senderTelegramId, reason);
                sendText(senderTelegramId, "✅ Заявка на сертификат #" + requestId + " отклонена.");
            }
        } catch (Exception e) {
            log.error("Error handling master text reply: {}", e.getMessage());
            sendText(senderTelegramId, "❌ Ошибка. Проверьте формат сообщения.");
        }
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    private void sendText(Long chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId).text(text).parseMode("HTML").build();
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to send message chatId={}: {}", chatId, e.getMessage());
        }
    }

    private void answerCallback(String callbackQueryId, String text) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId).text(text).build());
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback: {}", e.getMessage());
        }
    }
}