package ibrel.tgBeautyWebApp.web.controller.task;

import ibrel.tgBeautyWebApp.model.task.TaskDefinition;
import ibrel.tgBeautyWebApp.model.task.UserTaskProgress;
import ibrel.tgBeautyWebApp.service.task.UserTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class UserTaskController {

    private final UserTaskService userTaskService;

    /** Список всех активных заданий (публичный) */
    @GetMapping
    public ResponseEntity<List<TaskDefinition>> getAllActive() {
        return ResponseEntity.ok(userTaskService.getAllActive());
    }

    /** Прогресс пользователя по всем заданиям */
    @GetMapping("/progress")
    public ResponseEntity<List<UserTaskProgress>> getProgress(@RequestParam Long telegramId) {
        return ResponseEntity.ok(userTaskService.getProgress(telegramId));
    }

    /**
     * Отметить выполнение задания без порога (например, подписка на TG).
     * Клиент вызывает этот endpoint после совершения действия.
     */
    @PostMapping("/complete")
    public ResponseEntity<Void> markCompleted(
            @RequestParam Long telegramId,
            @RequestParam String taskCode) {
        // Для заданий без порога: инкрементируем до 1 и проверяем
        userTaskService.incrementProgress(telegramId, taskCode);
        userTaskService.checkAndComplete(telegramId, taskCode);
        return ResponseEntity.ok().build();
    }

    /**
     * Инициализировать прогресс заданий для нового пользователя.
     * Вызывается автоматически при создании пользователя,
     * но доступен и как отдельный endpoint (для ручной инициализации).
     */
    @PostMapping("/init")
    public ResponseEntity<Void> initProgress(@RequestParam Long telegramId) {
        userTaskService.initProgressForUser(telegramId);
        return ResponseEntity.ok().build();
    }
}
