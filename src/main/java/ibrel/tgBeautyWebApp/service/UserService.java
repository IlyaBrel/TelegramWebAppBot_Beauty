package ibrel.tgBeautyWebApp.service;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // 🔹 Базовые операции
    UserTG save(UserTG userTG);
    Optional<UserTG> findByTelegramId(Long telegramId);
    List<UserTG> findAll();
    void deleteByTelegramId(Long telegramId);
    // 🔹 Управление активностью
    void changeActivity(Long telegramId, boolean active);
    // 🔹 Управление ролями
    void changeRole(Long telegramId, UserRole role);
    // 🔹 Обновление данных
    UserTG updateUser(Long telegramId, UserTG updatedData);
    // 🔹 Взаимодействие
    boolean exists(Long telegramId);
    long countUsers();
}
