package ibrel.tgBeautyWebApp.service.bonus.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.points.PointTransaction;
import ibrel.tgBeautyWebApp.repository.bonus.PointTransactionRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.bonus.PointsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointsServiceImpl implements PointsService {

    private final UserRepository userRepo;
    private final PointTransactionRepository ptxRepo;

    @Override
    @Transactional
    public PointTransaction grant(Long telegramId, int amount,
                                  PointTransaction.PointReason reason, String taskType) {
        UserTG user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        user.setPoints(user.getPoints() + amount);
        userRepo.save(user);

        PointTransaction tx = PointTransaction.builder()
                .user(user)
                .amount(amount)
                .reason(reason)
                .taskType(taskType)
                .build();

        PointTransaction saved = ptxRepo.save(tx);
        log.info("Granted {} points to telegramId={} reason={}", amount, telegramId, reason);
        return saved;
    }

    @Override
    public List<PointTransaction> getHistory(Long telegramId) {
        return ptxRepo.findByUser_TelegramIdOrderByCreatedAtDesc(telegramId);
    }
}
