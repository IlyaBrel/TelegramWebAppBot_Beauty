package ibrel.tgBeautyWebApp.service.task.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.points.PointTransaction;
import ibrel.tgBeautyWebApp.model.task.TaskDefinition;
import ibrel.tgBeautyWebApp.model.task.UserTaskProgress;
import ibrel.tgBeautyWebApp.repository.task.TaskDefinitionRepository;
import ibrel.tgBeautyWebApp.repository.task.UserTaskProgressRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.bonus.PointsService;
import ibrel.tgBeautyWebApp.service.task.UserTaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTaskServiceImpl implements UserTaskService {

    private final UserRepository userRepo;
    private final TaskDefinitionRepository taskDefRepo;
    private final UserTaskProgressRepository progressRepo;
    private final PointsService pointsService;

    @Override
    @Transactional
    public void initProgressForUser(Long telegramId) {
        UserTG user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        List<TaskDefinition> activeTasks = taskDefRepo.findByActiveTrueOrderBySortOrderAsc();

        for (TaskDefinition task : activeTasks) {
            boolean alreadyExists = progressRepo
                    .findByUser_TelegramIdAndTaskDefinition_Code(telegramId, task.getCode())
                    .isPresent();

            if (!alreadyExists) {
                UserTaskProgress progress = UserTaskProgress.builder()
                        .user(user)
                        .taskDefinition(task)
                        .build();
                progressRepo.save(progress);
            }
        }

        log.info("Initialized task progress for telegramId={}, tasks={}", telegramId, activeTasks.size());
    }

    @Override
    @Transactional
    public void checkAndComplete(Long telegramId, String taskCode) {
        UserTaskProgress progress = progressRepo
                .findByUser_TelegramIdAndTaskDefinition_Code(telegramId, taskCode)
                .orElse(null);

        if (progress == null || Boolean.TRUE.equals(progress.getCompleted())) {
            return; // нет прогресса или уже выполнено
        }

        TaskDefinition task = progress.getTaskDefinition();
        boolean shouldComplete;

        if (task.getThreshold() != null && task.getThreshold() > 0) {
            // Задание с порогом: завершаем если прогресс >= порога
            shouldComplete = progress.getCurrentProgress() >= task.getThreshold();
        } else {
            // Задание без порога: выполняется однократно
            shouldComplete = progress.getCurrentProgress() >= 1;
        }

        if (shouldComplete) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progressRepo.save(progress);

            // Начислить баллы
            if (task.getPointsReward() > 0) {
                pointsService.grant(telegramId, task.getPointsReward(),
                        PointTransaction.PointReason.TASK_COMPLETED, task.getCode());
            }

            log.info("Task {} completed for telegramId={}, reward={} pts",
                    taskCode, telegramId, task.getPointsReward());
        }
    }

    @Override
    @Transactional
    public void incrementProgress(Long telegramId, String taskCode) {
        UserTaskProgress progress = progressRepo
                .findByUser_TelegramIdAndTaskDefinition_Code(telegramId, taskCode)
                .orElse(null);

        if (progress == null || Boolean.TRUE.equals(progress.getCompleted())) {
            return; // нет прогресса или уже выполнено
        }

        progress.setCurrentProgress(progress.getCurrentProgress() + 1);
        progressRepo.save(progress);

        log.debug("Incremented progress for telegramId={} task={} to {}",
                telegramId, taskCode, progress.getCurrentProgress());
    }

    @Override
    public List<UserTaskProgress> getProgress(Long telegramId) {
        return progressRepo.findByUser_TelegramId(telegramId);
    }

    @Override
    public List<TaskDefinition> getAllActive() {
        return taskDefRepo.findByActiveTrueOrderBySortOrderAsc();
    }
}
