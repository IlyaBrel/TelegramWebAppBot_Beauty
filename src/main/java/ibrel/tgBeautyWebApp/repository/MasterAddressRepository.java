package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterAddressRepository extends JpaRepository<MasterAddress, Long> {
    MasterAddress findByMasterId(Long masterId);
}
