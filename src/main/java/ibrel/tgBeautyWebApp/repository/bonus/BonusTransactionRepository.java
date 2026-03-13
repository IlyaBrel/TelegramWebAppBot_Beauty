package ibrel.tgBeautyWebApp.repository.bonus;

import ibrel.tgBeautyWebApp.model.bonus.BonusTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {

    List<BonusTransaction> findByBonusAccount_IdOrderByCreatedAtDesc(Long bonusAccountId);
}
