package ibrel.tgBeautyWebApp.repository.bonus;

import ibrel.tgBeautyWebApp.model.bonus.BonusAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BonusAccountRepository extends JpaRepository<BonusAccount, Long> {

    Optional<BonusAccount> findByUser_TelegramId(Long telegramId);

    Optional<BonusAccount> findByUser_Id(Long userId);
}
