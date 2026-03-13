package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterServiceWorkRepository extends JpaRepository<MasterServiceWork, Long> {

    List<MasterServiceWork> findByMaster_IdOrderByNameAsc(Long masterId);

    List<MasterServiceWork> findByMaster_IdAndActiveTrueOrderByNameAsc(Long masterId);

    boolean existsByMaster_IdAndId(Long masterId, Long serviceId);
}