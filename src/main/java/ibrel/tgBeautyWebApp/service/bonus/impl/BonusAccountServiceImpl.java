package ibrel.tgBeautyWebApp.service.bonus.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.bonus.BonusAccount;
import ibrel.tgBeautyWebApp.model.bonus.BonusTransaction;
import ibrel.tgBeautyWebApp.model.bonus.BonusTransaction.TransactionSource;
import ibrel.tgBeautyWebApp.model.bonus.BonusTransaction.TransactionType;
import ibrel.tgBeautyWebApp.repository.bonus.BonusAccountRepository;
import ibrel.tgBeautyWebApp.repository.bonus.BonusTransactionRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.bonus.BonusAccountService;
import ibrel.tgBeautyWebApp.service.settings.AppSettingsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonusAccountServiceImpl implements BonusAccountService {

    private final BonusAccountRepository accountRepo;
    private final BonusTransactionRepository txRepo;
    private final UserRepository userRepo;
    private final AppSettingsService settingsService;

    @Override
    @Transactional
    public BonusAccount createForUser(Long userId) {
        UserTG user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));
        BonusAccount account = BonusAccount.builder().user(user).build();
        return accountRepo.save(account);
    }

    @Override
    public BonusAccount getByTelegramId(Long telegramId) {
        return accountRepo.findByUser_TelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "BonusAccount not found for telegramId=" + telegramId));
    }

    @Override
    @Transactional
    public BonusTransaction credit(Long telegramId, BigDecimal amount,
                                   TransactionSource source, Long orderId, String comment) {
        BonusAccount account = getByTelegramId(telegramId);
        account.setBalance(account.getBalance().add(amount));
        accountRepo.save(account);

        BonusTransaction tx = BonusTransaction.builder()
                .bonusAccount(account)
                .amount(amount)
                .type(TransactionType.CREDIT)
                .source(source)
                .orderId(orderId)
                .comment(comment)
                .build();
        log.info("CREDIT {} rub to telegramId={} source={}", amount, telegramId, source);
        return txRepo.save(tx);
    }

    @Override
    @Transactional
    public BonusTransaction debit(Long telegramId, BigDecimal amount,
                                  TransactionSource source, Long orderId, String comment) {
        BonusAccount account = getByTelegramId(telegramId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient bonus balance: " + account.getBalance() + " < " + amount);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepo.save(account);

        BonusTransaction tx = BonusTransaction.builder()
                .bonusAccount(account)
                .amount(amount)
                .type(TransactionType.DEBIT)
                .source(source)
                .orderId(orderId)
                .comment(comment)
                .build();
        log.info("DEBIT {} rub from telegramId={} source={}", amount, telegramId, source);
        return txRepo.save(tx);
    }

    @Override
    @Transactional
    public BonusTransaction convertPointsToBonus(Long telegramId) {
        UserTG user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        int points = user.getPoints();
        if (points <= 0) {
            throw new IllegalStateException("No points to convert");
        }

        BigDecimal rate = settingsService.get().getPointsToRubleRate();
        BigDecimal rubles = BigDecimal.valueOf(points)
                .divide(rate, 2, RoundingMode.FLOOR);

        user.setPoints(0);
        userRepo.save(user);

        return credit(telegramId, rubles, TransactionSource.POINTS_CONVERSION, null,
                "Конвертация " + points + " баллов по курсу " + rate + " балл/руб");
    }

    @Override
    public List<BonusTransaction> getHistory(Long telegramId) {
        BonusAccount account = getByTelegramId(telegramId);
        return txRepo.findByBonusAccount_IdOrderByCreatedAtDesc(account.getId());
    }
}
