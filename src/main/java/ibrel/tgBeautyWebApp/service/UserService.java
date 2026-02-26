package ibrel.tgBeautyWebApp.service;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserTG save(UserTG userTG);
    Optional<UserTG> findByTelegramId(Long telegramId);
    List<UserTG> findAll();
    void deleteByTelegramId(Long telegramId);

    // управление активностью/ролями
    void changeActivity(Long telegramId, boolean active, Long requesterTelegramId);
    void changeRole(Long telegramId, UserRole role, Long requesterTelegramId);

    UserTG updateUser(Long telegramId, UserTG updatedData, Long requesterTelegramId);

    boolean exists(Long telegramId);
    long countUsers();

    // админские операции
    boolean hasAnyAdmin();
    List<UserTG> listPendingActivation();
    UserTG activateUser(Long targetTelegramId, Long adminTelegramId);
    UserTG createByAdmin(UserTG userTG, Long adminTelegramId);
}
