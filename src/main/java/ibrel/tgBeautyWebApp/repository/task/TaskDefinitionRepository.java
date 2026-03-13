package ibrel.tgBeautyWebApp.repository.task;

import ibrel.tgBeautyWebApp.model.task.TaskDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskDefinitionRepository extends JpaRepository<TaskDefinition, Long> {

    Optional<TaskDefinition> findByCode(String code);

    List<TaskDefinition> findByActiveTrueOrderBySortOrderAsc();
}
