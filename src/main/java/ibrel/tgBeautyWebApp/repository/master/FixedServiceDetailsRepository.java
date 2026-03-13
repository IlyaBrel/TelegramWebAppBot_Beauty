package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FixedServiceDetailsRepository extends JpaRepository<FixedServiceDetails, Long> {
    Optional<FixedServiceDetails> findByService_Id(Long serviceId);
    void deleteByService_Id(Long serviceId);
}