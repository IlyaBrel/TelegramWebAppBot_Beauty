package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterPersonalDataRepository extends JpaRepository<MasterPersonalData, Long> {
    Optional<MasterPersonalData> findByMasterId(Long masterId);

    void deleteByMasterId(Long masterId);
}