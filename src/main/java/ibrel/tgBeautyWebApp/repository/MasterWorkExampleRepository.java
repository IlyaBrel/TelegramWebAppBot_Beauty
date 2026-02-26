package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.MasterWorkExample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterWorkExampleRepository extends JpaRepository<MasterWorkExample, Long> {
    List<MasterWorkExample> findByMasterId(Long masterId);
}
