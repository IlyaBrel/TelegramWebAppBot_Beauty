package ibrel.tgBeautyWebApp;

import ibrel.tgBeautyWebApp.service.user.impl.UserCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotManagement implements LongPollingSingleThreadUpdateConsumer {

    private final UserCommandHandler userCommandHandler;

    @Override
    public void consume(Update update) {

        // ── Callback-кнопки (Одобрить / Отклонить запись или сертификат) ──────
        if (update.hasCallbackQuery()) {
            userCommandHandler.handleCallback(update);
            return;
        }

        // ── Текстовые сообщения ───────────────────────────────────────────────
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText().trim();

            if (text.equals("/start")) {
                userCommandHandler.handleStart(update);
                return;
            }

            // Мастер пишет причину отклонения:
            //   reject_reason:{appointmentId} <текст>
            //   cert_reject_reason:{requestId} <текст>
            if (text.startsWith("reject_reason:") || text.startsWith("cert_reject_reason:")) {
                userCommandHandler.handleMasterTextReply(update);
                return;
            }

            userCommandHandler.handleUnknown(update);
        }
    }
}