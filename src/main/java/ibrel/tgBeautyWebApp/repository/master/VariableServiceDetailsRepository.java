package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariableServiceDetailsRepository extends JpaRepository<VariableServiceDetails, Long> {
    List<VariableServiceDetails> findByService_IdOrderByFactorNameAsc(Long serviceId);
    void deleteByService_Id(Long serviceId);
}