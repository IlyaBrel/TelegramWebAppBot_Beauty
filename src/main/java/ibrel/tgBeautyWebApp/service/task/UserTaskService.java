package ibrel.tgBeautyWebApp.service.task;

import ibrel.tgBeautyWebApp.model.task.TaskDefinition;
import ibrel.tgBeautyWebApp.model.task.UserTaskProgress;

import java.util.List;

public interface UserTaskService {

    /** Инициализировать прогресс по всем активным заданиям для нового пользователя */
    void initProgressForUser(Long telegramId);

    /**
     * Проверить прогресс по заданию и отметить выполненным если порог достигнут.
     * Вызывается из любого места системы (после отзыва, приглашения и т.д.)
     */
    void checkAndComplete(Long telegramId, String taskCode);

    /** Инкрементировать прогресс задания с порогом */
    void incrementProgress(Long telegramId, String taskCode);

    List<UserTaskProgress> getProgress(Long telegramId);

    List<TaskDefinition> getAllActive();
}
