package ibrel.tgBeautyWebApp.service.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.repository.UserRepository;
import ibrel.tgBeautyWebApp.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    @Override
    @Transactional
    public UserTG save(UserTG userTG) {
        userTG.setUpdatedAt(LocalDateTime.now());
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
    public void changeActivity(Long telegramId, boolean active, Long requesterTelegramId) {
        Assert.notNull(requesterTelegramId, "requesterTelegramId must not be null");
        UserTG requester = userRepo.findByTelegramId(requesterTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        if (!UserRole.ADMIN.equals(requester.getRole())) {
            throw new IllegalArgumentException("Only admin can change activity");
        }

        userRepo.findByTelegramId(telegramId).ifPresent(user -> {
            user.setActive(active);
            user.setUpdatedAt(LocalDateTime.now());
            userRepo.save(user);
        });
    }

    @Override
    public void changeRole(Long telegramId, UserRole role, Long requesterTelegramId) {
        Assert.notNull(requesterTelegramId, "requesterTelegramId must not be null");
        UserTG requester = userRepo.findByTelegramId(requesterTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        if (!UserRole.ADMIN.equals(requester.getRole())) {
            throw new IllegalArgumentException("Only admin can change roles");
        }

        userRepo.findByTelegramId(telegramId).ifPresent(user -> {
            user.setRole(role);
            user.setUpdatedAt(LocalDateTime.now());
            userRepo.save(user);
        });
    }

    @Override
    @Transactional
    public UserTG updateUser(Long telegramId, UserTG updatedData, Long requesterTelegramId) {
        Assert.notNull(requesterTelegramId, "requesterTelegramId must not be null");
        UserTG requester = userRepo.findByTelegramId(requesterTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        UserTG target = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isAdmin = UserRole.ADMIN.equals(requester.getRole());
        boolean isSelf = requesterTelegramId.equals(telegramId);

        if (!(isAdmin || isSelf)) {
            throw new IllegalArgumentException("Not allowed to update this user");
        }

        if (updatedData.getUsername() != null) target.setUsername(updatedData.getUsername());
        if (updatedData.getFirstName() != null) target.setFirstName(updatedData.getFirstName());
        if (updatedData.getLastName() != null) target.setLastName(updatedData.getLastName());
        if (isAdmin && updatedData.getRole() != null) target.setRole(updatedData.getRole());
        if (isAdmin && updatedData.getActive() != null) target.setActive(updatedData.getActive());

        target.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(target);
    }

    @Override
    public boolean exists(Long telegramId) {
        return userRepo.findByTelegramId(telegramId).isPresent();
    }

    @Override
    public long countUsers() {
        return userRepo.count();
    }

    @Override
    public boolean hasAnyAdmin() {
        return userRepo.countByRole(UserRole.ADMIN) > 0;
    }

    @Override
    public List<UserTG> listPendingActivation() {
        return userRepo.findByActiveFalse();
    }

    @Override
    @Transactional
    public UserTG activateUser(Long targetTelegramId, Long adminTelegramId) {
        UserTG admin = userRepo.findByTelegramId(adminTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        if (!UserRole.ADMIN.equals(admin.getRole())) {
            throw new IllegalArgumentException("Only admin can activate users");
        }

        UserTG target = userRepo.findByTelegramId(targetTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        target.setActive(true);
        target.setCreatedByAdminTelegramId(adminTelegramId);
        target.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(target);
    }

    @Override
    @Transactional
    public UserTG createByAdmin(UserTG userTG, Long adminTelegramId) {
        UserTG admin = userRepo.findByTelegramId(adminTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        if (!UserRole.ADMIN.equals(admin.getRole())) {
            throw new IllegalArgumentException("Only admin can create users");
        }

        userTG.setActive(true);
        userTG.setCreatedByAdminTelegramId(adminTelegramId);
        userTG.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(userTG);
    }
}
