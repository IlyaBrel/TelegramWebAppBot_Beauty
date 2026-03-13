package ibrel.tgBeautyWebApp.repository.task;

import ibrel.tgBeautyWebApp.model.task.UserTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTaskProgressRepository extends JpaRepository<UserTaskProgress, Long> {

    List<UserTaskProgress> findByUser_TelegramId(Long telegramId);

    Optional<UserTaskProgress> findByUser_TelegramIdAndTaskDefinition_Code(Long telegramId, String code);

    List<UserTaskProgress> findByUser_TelegramIdAndCompletedFalse(Long telegramId);
}
