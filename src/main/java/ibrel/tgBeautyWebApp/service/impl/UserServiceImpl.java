package ibrel.tgBeautyWebApp.service.impl;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.repository.UserRepository;
import ibrel.tgBeautyWebApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;

    @Override
    public UserTG save(UserTG userTG) {
        return userRepo.save(userTG);
    }

    @Override
    public Optional<UserTG> findByTelegramId(Long telegramId) {
        return userRepo.findByTelegramId(telegramId);
    }

    @Override
    public List<UserTG> findAll() {
        return userRepo.findAll();
    }

    @Override
    public void deleteByTelegramId(Long telegramId) {
        userRepo.findByTelegramId(telegramId).ifPresent(userRepo::delete);
    }

    @Override
    public void changeActivity(Long telegramId, boolean active) {
        userRepo.findByTelegramId(telegramId).ifPresent(user -> {
            user.setActive(active);
            userRepo.save(user);
        });
    }

    @Override
    public void changeRole(Long telegramId, UserRole role) {
        userRepo.findByTelegramId(telegramId).ifPresent(user -> {
            user.setRole(role);
            userRepo.save(user);
        });
    }

    @Override
    public UserTG updateUser(Long telegramId, UserTG updatedData) {
        return userRepo.findByTelegramId(telegramId)
                .map(user -> {
                    if (updatedData.getUsername() != null) user.setUsername(updatedData.getUsername());
                    if (updatedData.getFirstName() != null) user.setFirstName(updatedData.getFirstName());
                    if (updatedData.getLastName() != null) user.setLastName(updatedData.getLastName());
                    if (updatedData.getRole() != null) user.setRole(updatedData.getRole());
                    if (updatedData.getActive() != null) user.setActive(updatedData.getActive());
                    return userRepo.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean exists(Long telegramId) {
        return userRepo.findByTelegramId(telegramId).isPresent();
    }

    @Override
    public long countUsers() {
        return userRepo.count();
    }
}

