package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterAddressRepository extends JpaRepository<MasterAddress, Long> {
    Optional<MasterAddress> findByMasterId(Long masterId);
    void deleteByMasterId(Long masterId);
}
