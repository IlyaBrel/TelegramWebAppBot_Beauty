package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterPersonalDataRepository extends JpaRepository<MasterPersonalData, Long> {
}
