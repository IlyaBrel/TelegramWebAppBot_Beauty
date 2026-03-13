package ibrel.tgBeautyWebApp.service.tg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService {

    private final TelegramClient telegramClient;

    // ── Базовая отправка ──────────────────────────────────────────────────────

    public void sendMessage(Long telegramId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(telegramId)
                .text(text)
                .parseMode("HTML")
                .build();
        execute(telegramId, msg);
    }

    public void sendMessageWithButtons(Long telegramId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage msg = SendMessage.builder()
                .chatId(telegramId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();
        execute(telegramId, msg);
    }

    // ── Уведомления пользователю ──────────────────────────────────────────────

    /** Аккаунт активирован */
    public void notifyActivated(Long telegramId) {
        sendMessage(telegramId,
                "✅ <b>Ваш аккаунт активирован!</b>\n\nТеперь вы можете пользоваться сервисом.");
    }

    /** Заказ создан — ожидает подтверждения мастера */
    public void notifyAppointmentPending(Long telegramId, String masterName, String date, String time) {
        sendMessage(telegramId, String.format(
                "⏳ <b>Заявка отправлена!</b>\n\n" +
                        "👩‍🎨 Мастер: %s\n" +
                        "📅 Дата: %s\n" +
                        "🕐 Время: %s\n\n" +
                        "Ожидайте подтверждения от мастера.",
                masterName, date, time));
    }

    /** Заказ подтверждён мастером */
    public void notifyAppointmentConfirmed(Long telegramId, String masterName, String date, String time) {
        sendMessage(telegramId, String.format(
                "✅ <b>Запись подтверждена!</b>\n\n" +
                        "👩‍🎨 Мастер: %s\n" +
                        "📅 Дата: %s\n" +
                        "🕐 Время: %s\n\n" +
                        "Ждём вас!",
                masterName, date, time));
    }

    /** Заказ отклонён мастером */
    public void notifyAppointmentRejected(Long telegramId, String masterName, String reason) {
        sendMessage(telegramId, String.format(
                "❌ <b>Запись отклонена</b>\n\n" +
                        "👩‍🎨 Мастер: %s\n" +
                        "💬 Причина: %s\n\n" +
                        "Вы можете выбрать другое время.",
                masterName, reason != null ? reason : "не указана"));
    }

    /** Заказ отменён */
    public void notifyAppointmentCancelled(Long telegramId) {
        sendMessage(telegramId, "❌ <b>Ваша запись отменена.</b>");
    }

    /** Напоминание перед записью */
    public void notifyReminder(Long telegramId, String masterName, String date, String time, int hoursLeft) {
        sendMessage(telegramId, String.format(
                "⏰ <b>Напоминание о записи</b>\n\n" +
                        "👩‍🎨 Мастер: %s\n" +
                        "📅 Дата: %s\n" +
                        "🕐 Время: %s\n\n" +
                        "До записи осталось %d %s.",
                masterName, date, time, hoursLeft, pluralHours(hoursLeft)));
    }

    /** Запрос на отзыв после завершения */
    public void notifyReviewRequest(Long telegramId, Long appointmentId, String masterName) {
        String text = String.format(
                "⭐ <b>Как прошла запись?</b>\n\n" +
                        "Вы были у мастера %s.\n" +
                        "Пожалуйста, оставьте отзыв — это займёт 1 минуту.",
                masterName);

        InlineKeyboardButton btn = InlineKeyboardButton.builder()
                .text("✍️ Оставить отзыв")
                .callbackData("review:" + appointmentId)
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(btn)))
                .build();

        sendMessageWithButtons(telegramId, text, keyboard);
    }

    /** Сертификат выдан */
    public void notifyCertificateIssued(Long telegramId, String masterName, String code, String services) {
        sendMessage(telegramId, String.format(
                "🎁 <b>Вам подарили сертификат!</b>\n\n" +
                        "👩‍🎨 От мастера: %s\n" +
                        "💆 Услуги: %s\n" +
                        "🔑 Код: <code>%s</code>\n\n" +
                        "Выберите удобное время и запишитесь.",
                masterName, services, code));
    }

    // ── Уведомления мастеру ───────────────────────────────────────────────────

    /**
     * Новая заявка от пользователя — мастеру приходят кнопки Подтвердить / Отклонить.
     */
    public void notifyMasterNewAppointment(Long masterTelegramId,
                                           Long appointmentId,
                                           String clientName,
                                           String date,
                                           String time,
                                           String services) {
        String text = String.format(
                "📥 <b>Новая заявка на запись!</b>\n\n" +
                        "👤 Клиент: %s\n" +
                        "📅 Дата: %s\n" +
                        "🕐 Время: %s\n" +
                        "💆 Услуги: %s",
                clientName, date, time, services);

        InlineKeyboardButton confirm = InlineKeyboardButton.builder()
                .text("✅ Подтвердить")
                .callbackData("apt_confirm:" + appointmentId)
                .build();

        InlineKeyboardButton reject = InlineKeyboardButton.builder()
                .text("❌ Отклонить")
                .callbackData("apt_reject:" + appointmentId)
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(confirm, reject)))
                .build();

        sendMessageWithButtons(masterTelegramId, text, keyboard);
    }

    /** Заказ отменён пользователем — уведомить мастера */
    public void notifyMasterAppointmentCancelled(Long masterTelegramId,
                                                 String clientName,
                                                 String date,
                                                 String time) {
        sendMessage(masterTelegramId, String.format(
                "❌ <b>Запись отменена клиентом</b>\n\n" +
                        "👤 Клиент: %s\n" +
                        "📅 Дата: %s, 🕐 %s",
                clientName, date, time));
    }

    // ── Вспомогательное ───────────────────────────────────────────────────────

    private void execute(Long telegramId, SendMessage msg) {
        try {
            telegramClient.execute(msg);
            log.info("Sent message to telegramId={}", telegramId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to telegramId={}: {}", telegramId, e.getMessage());
            throw new RuntimeException("Telegram send failed: " + e.getMessage(), e);
        }
    }

    private String pluralHours(int hours) {
        if (hours == 1) return "час";
        if (hours >= 2 && hours <= 4) return "часа";
        return "часов";
    }
}