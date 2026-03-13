package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterAddressRepository extends JpaRepository<MasterAddress, Long> {
    Optional<MasterAddress> findByMaster_Id(Long masterId);
    void deleteByMaster_Id(Long masterId);
}