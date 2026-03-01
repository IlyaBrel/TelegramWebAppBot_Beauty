package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface WorkSlotRepository extends JpaRepository<WorkSlot, Long> {
    List<WorkSlot> findByMasterId(Long masterId);

    List<WorkSlot> findByMasterIdAndDayOfWeek(Long masterId, String dayOfWeek);

    List<WorkSlot> findByMasterIdAndDayOfWeekAndStartTimeBetween(Long masterId, String dayOfWeek, LocalTime from, LocalTime to);
}
