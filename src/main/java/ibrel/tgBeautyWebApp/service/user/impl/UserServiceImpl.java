package ibrel.tgBeautyWebApp.service.user.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.master.MasterService;
import ibrel.tgBeautyWebApp.service.tg.TelegramNotificationService;
import ibrel.tgBeautyWebApp.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository                userRepo;
    private final TelegramNotificationService   telegramNotificationService;
    // @Lazy — разрываем циклическую зависимость UserService ↔ MasterService
    private final MasterService                 masterService;

    public UserServiceImpl(UserRepository userRepo,
                           TelegramNotificationService telegramNotificationService,
                           @Lazy MasterService masterService) {
        this.userRepo                   = userRepo;
        this.telegramNotificationService = telegramNotificationService;
        this.masterService              = masterService;
    }

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
    @Transactional
    public void deleteByTelegramId(Long telegramId) {
        UserTG user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        if (Boolean.TRUE.equals(user.getIsInitialAdmin())) {
            throw new IllegalArgumentException("Cannot delete initial admin");
        }
        userRepo.delete(user);
        log.info("Deleted user telegramId={}", telegramId);
    }

    @Override
    @Transactional
    public void changeActivity(Long telegramId, boolean active, Long requesterTelegramId) {
        Assert.notNull(requesterTelegramId, "requesterTelegramId must not be null");
        requireAdmin(requesterTelegramId);

        UserTG user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
    }

    @Override
    @Transactional
    public void changeRole(Long telegramId, UserRole role, Long requesterTelegramId) {
        Assert.notNull(requesterTelegramId, "requesterTelegramId must not be null");
        requireAdmin(requesterTelegramId);

        UserTG user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        if (Boolean.TRUE.equals(user.getIsInitialAdmin()) && role != UserRole.ADMIN) {
            throw new IllegalArgumentException("Cannot change role of initial admin");
        }

        UserRole previousRole = user.getRole();
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        log.info("Changed role telegramId={} {} → {} by admin={}", telegramId, previousRole, role, requesterTelegramId);

        // ── Авто-создание мастера при назначении роли MASTER ─────────────────
        if (UserRole.MASTER.equals(role) && !UserRole.MASTER.equals(previousRole)) {
            try {
                masterService.createFromUser(user, requesterTelegramId);
                log.info("Auto-created master profile for telegramId={}", telegramId);
            } catch (Exception e) {
                // Не откатываем смену роли — мастера можно создать вручную
                log.warn("Could not auto-create master for telegramId={}: {}", telegramId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public UserTG updateUser(Long telegramId, UserTG updatedData, Long requesterTelegramId) {
        Assert.notNull(requesterTelegramId, "requesterTelegramId must not be null");

        UserTG requester = userRepo.findByTelegramId(requesterTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        UserTG target = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        boolean isAdmin = UserRole.ADMIN.equals(requester.getRole());
        boolean isSelf  = requesterTelegramId.equals(telegramId);

        if (!isAdmin && !isSelf) {
            throw new IllegalArgumentException("Not allowed to update this user");
        }

        if (updatedData.getUsername()  != null) target.setUsername(updatedData.getUsername());
        if (updatedData.getFirstName() != null) target.setFirstName(updatedData.getFirstName());
        if (updatedData.getLastName()  != null) target.setLastName(updatedData.getLastName());

        if (isAdmin) {
            if (updatedData.getRole()   != null) target.setRole(updatedData.getRole());
            if (updatedData.getActive() != null) target.setActive(updatedData.getActive());
        }

        target.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(target);
    }

    @Override
    public boolean exists(Long telegramId) {
        return userRepo.existsByTelegramId(telegramId);
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
        requireAdmin(adminTelegramId);

        UserTG target = userRepo.findByTelegramId(targetTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + targetTelegramId));

        if (Boolean.TRUE.equals(target.getActive())) return target;

        target.setActive(true);
        target.setCreatedByAdminTelegramId(adminTelegramId);
        target.setUpdatedAt(LocalDateTime.now());
        UserTG saved = userRepo.save(target);

        try {
            telegramNotificationService.notifyActivated(targetTelegramId);
        } catch (Exception e) {
            log.warn("Could not send activation notification to telegramId={}: {}", targetTelegramId, e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional
    public UserTG createByAdmin(UserTG userTG, Long adminTelegramId) {
        requireAdmin(adminTelegramId);

        if (userRepo.existsByTelegramId(userTG.getTelegramId())) {
            throw new IllegalArgumentException("User already exists: " + userTG.getTelegramId());
        }

        userTG.setActive(true);
        userTG.setCreatedByAdminTelegramId(adminTelegramId);
        userTG.setCreatedAt(LocalDateTime.now());
        userTG.setUpdatedAt(LocalDateTime.now());

        return userRepo.save(userTG);
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    private void requireAdmin(Long telegramId) {
        UserTG requester = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found: " + telegramId));
        if (!UserRole.ADMIN.equals(requester.getRole())) {
            throw new IllegalArgumentException("Only admin can perform this action");
        }
    }
}