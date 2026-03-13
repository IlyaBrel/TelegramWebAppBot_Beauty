package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterPersonalDataRepository extends JpaRepository<MasterPersonalData, Long> {
    Optional<MasterPersonalData> findByMaster_Id(Long masterId);
    void deleteByMaster_Id(Long masterId);
}