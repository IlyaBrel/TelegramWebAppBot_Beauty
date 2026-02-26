package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterServiceWorkRepository extends JpaRepository<MasterServiceWork, Long> {
    List<MasterServiceWork> findByMasterId(Long masterId);
}