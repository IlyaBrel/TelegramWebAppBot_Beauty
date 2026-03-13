package ibrel.tgBeautyWebApp.web.controller.bonus;

import ibrel.tgBeautyWebApp.model.bonus.BonusAccount;
import ibrel.tgBeautyWebApp.model.bonus.BonusTransaction;
import ibrel.tgBeautyWebApp.model.bonus.BonusTransaction.TransactionSource;
import ibrel.tgBeautyWebApp.model.points.PointTransaction;
import ibrel.tgBeautyWebApp.service.bonus.BonusAccountService;
import ibrel.tgBeautyWebApp.service.bonus.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
public class BonusController {

    private final BonusAccountService bonusAccountService;
    private final PointsService pointsService;

    // ── Бонусный счёт ─────────────────────────────────────────────────────────

    /** Получить бонусный счёт пользователя */
    @GetMapping("/account")
    public ResponseEntity<BonusAccount> getAccount(@RequestParam Long telegramId) {
        return ResponseEntity.ok(bonusAccountService.getByTelegramId(telegramId));
    }

    /** История бонусных транзакций */
    @GetMapping("/account/history")
    public ResponseEntity<List<BonusTransaction>> getBonusHistory(@RequestParam Long telegramId) {
        return ResponseEntity.ok(bonusAccountService.getHistory(telegramId));
    }

    /** Конвертировать баллы в рубли на бонусный счёт */
    @PostMapping("/account/convert-points")
    public ResponseEntity<BonusTransaction> convertPoints(@RequestParam Long telegramId) {
        return ResponseEntity.ok(bonusAccountService.convertPointsToBonus(telegramId));
    }

    /** Пополнить бонусный счёт (для администратора) */
    @PostMapping("/account/credit")
    public ResponseEntity<BonusTransaction> credit(
            @RequestParam Long telegramId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "ADMIN_TOPUP") TransactionSource source,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String comment) {
        BonusTransaction tx = bonusAccountService.credit(telegramId, amount, source, orderId, comment);
        return ResponseEntity.ok(tx);
    }

    /** Списать с бонусного счёта */
    @PostMapping("/account/debit")
    public ResponseEntity<BonusTransaction> debit(
            @RequestParam Long telegramId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "ORDER_PAYMENT") TransactionSource source,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String comment) {
        BonusTransaction tx = bonusAccountService.debit(telegramId, amount, source, orderId, comment);
        return ResponseEntity.ok(tx);
    }

    // ── Баллы ─────────────────────────────────────────────────────────────────

    /** История начислений баллов */
    @GetMapping("/points/history")
    public ResponseEntity<List<PointTransaction>> getPointsHistory(@RequestParam Long telegramId) {
        return ResponseEntity.ok(pointsService.getHistory(telegramId));
    }

    /** Начислить баллы вручную (администратор) */
    @PostMapping("/points/grant")
    public ResponseEntity<PointTransaction> grantPoints(
            @RequestParam Long telegramId,
            @RequestParam int amount,
            @RequestParam(required = false, defaultValue = "ADMIN_GRANT") PointTransaction.PointReason reason,
            @RequestParam(required = false) String comment) {
        PointTransaction tx = pointsService.grant(telegramId, amount, reason, comment);
        return ResponseEntity.ok(tx);
    }
}
