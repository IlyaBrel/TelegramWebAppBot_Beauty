package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.UserTG;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTG, Long> {
    Optional<UserTG> findByTelegramId(Long telegramId);
}