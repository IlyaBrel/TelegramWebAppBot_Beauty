package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkSlotRepository extends JpaRepository<WorkSlot, Long> {
    List<WorkSlot> findByMasterId(Long masterId);
}