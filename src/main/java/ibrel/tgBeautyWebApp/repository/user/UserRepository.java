package ibrel.tgBeautyWebApp.repository.user;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTG, Long> {

    Optional<UserTG> findByTelegramId(Long telegramId);

    boolean existsByTelegramId(Long telegramId);

    long countByRole(UserRole role);

    List<UserTG> findByActiveFalse();

    List<UserTG> findByRole(UserRole role);
}