package ibrel.tgBeautyWebApp.web.controller.tg;

import ibrel.tgBeautyWebApp.service.tg.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/telegram")
@RequiredArgsConstructor
public class TelegramMessageController {

    private final TelegramNotificationService telegramService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest req) {
        telegramService.sendMessage(req.getTelegramId(), req.getMessage());
        return ResponseEntity.ok().build();
    }

    public record SendMessageRequest(Long telegramId, String message) {
        public Long getTelegramId() { return telegramId; }
        public String getMessage() { return message; }
    }
}