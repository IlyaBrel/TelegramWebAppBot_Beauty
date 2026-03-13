package ibrel.tgBeautyWebApp.service.bonus;

import ibrel.tgBeautyWebApp.model.bonus.BonusAccount;
import ibrel.tgBeautyWebApp.model.bonus.BonusTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface BonusAccountService {

    BonusAccount getByTelegramId(Long telegramId);

    /** Создать бонусный счёт для нового пользователя */
    BonusAccount createForUser(Long userId);

    /** Пополнение баланса */
    BonusTransaction credit(Long telegramId, BigDecimal amount,
                            BonusTransaction.TransactionSource source,
                            Long orderId, String comment);

    /** Списание баланса (бросает исключение если недостаточно средств) */
    BonusTransaction debit(Long telegramId, BigDecimal amount,
                           BonusTransaction.TransactionSource source,
                           Long orderId, String comment);

    /** Конвертация баллов пользователя в рубли на бонусный счёт (вручную) */
    BonusTransaction convertPointsToBonus(Long telegramId);

    List<BonusTransaction> getHistory(Long telegramId);
}
