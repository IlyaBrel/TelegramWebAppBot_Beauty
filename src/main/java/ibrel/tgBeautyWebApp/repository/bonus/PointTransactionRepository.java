package ibrel.tgBeautyWebApp.repository.bonus;

import ibrel.tgBeautyWebApp.model.points.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByUser_TelegramIdOrderByCreatedAtDesc(Long telegramId);
}
