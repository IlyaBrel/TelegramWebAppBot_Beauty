package ibrel.tgBeautyWebApp.service.referral.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.referral.ReferralService;
import ibrel.tgBeautyWebApp.service.task.UserTaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralServiceImpl implements ReferralService {

    private final UserRepository userRepo;
    private final UserTaskService userTaskService;

    @Override
    public String generateInviteCode() {
        // 6 символов из UUID без дефисов, верхний регистр
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String candidate = uuid.substring(0, 6);

        // Проверяем уникальность (крайне маловероятный коллизий при 36^6 = ~2.2 млрд вариантов)
        int attempts = 0;
        while (userRepo.findAll().stream()
                .anyMatch(u -> candidate.equals(u.getInviteCode())) && attempts < 10) {
            attempts++;
        }
        return uuid.substring(attempts, attempts + 6);
    }

    @Override
    @Transactional
    public void handleReferral(Long newUserTelegramId, String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) return;

        UserTG referrer = userRepo.findAll().stream()
                .filter(u -> inviteCode.equalsIgnoreCase(u.getInviteCode()))
                .findFirst()
                .orElse(null);

        if (referrer == null) {
            log.warn("Referral invite code not found: {}", inviteCode);
            return;
        }

        UserTG newUser = userRepo.findByTelegramId(newUserTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + newUserTelegramId));

        // Нельзя пригласить самого себя
        if (referrer.getTelegramId().equals(newUserTelegramId)) return;

        newUser.setInvitedBy(referrer);
        userRepo.save(newUser);

        referrer.setInvitedUsersCount(referrer.getInvitedUsersCount() + 1);
        userRepo.save(referrer);

        // Проверить задание INVITE_FRIENDS у пригласившего
        userTaskService.incrementProgress(referrer.getTelegramId(), "INVITE_FRIENDS");
        userTaskService.checkAndComplete(referrer.getTelegramId(), "INVITE_FRIENDS");

        log.info("Referral registered: newUser={} invited by telegramId={}",
                newUserTelegramId, referrer.getTelegramId());
    }

    @Override
    @Transactional
    public void handleOrderCompleted(Long clientTelegramId) {
        UserTG client = userRepo.findByTelegramId(clientTelegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + clientTelegramId));

        UserTG referrer = client.getInvitedBy();
        if (referrer == null) return;

        referrer.setInvitedUsersOrdersCount(referrer.getInvitedUsersOrdersCount() + 1);
        userRepo.save(referrer);

        // Проверить задание FRIENDS_COMPLETE_ORDERS у пригласившего
        userTaskService.incrementProgress(referrer.getTelegramId(), "FRIENDS_COMPLETE_ORDERS");
        userTaskService.checkAndComplete(referrer.getTelegramId(), "FRIENDS_COMPLETE_ORDERS");

        log.info("Order completed referral update: client={} referrer={}",
                clientTelegramId, referrer.getTelegramId());
    }
}
