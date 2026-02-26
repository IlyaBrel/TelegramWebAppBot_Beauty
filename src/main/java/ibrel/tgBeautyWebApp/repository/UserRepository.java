package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTG, Long> {
    Optional<UserTG> findByTelegramId(Long telegramId);
    long countByRole(UserRole role);
    List<UserTG> findByActiveFalse(); // для списка ожидающих активации
}
