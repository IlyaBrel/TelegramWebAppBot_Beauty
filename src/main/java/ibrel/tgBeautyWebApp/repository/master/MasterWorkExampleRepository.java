package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.MasterWorkExample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterWorkExampleRepository extends JpaRepository<MasterWorkExample, Long> {
    List<MasterWorkExample> findByMaster_IdOrderByCreatedAtDesc(Long masterId);
    boolean existsByMaster_IdAndId(Long masterId, Long id);
}