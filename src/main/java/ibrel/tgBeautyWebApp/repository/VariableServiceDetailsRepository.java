package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariableServiceDetailsRepository extends JpaRepository<VariableServiceDetails, Long> {
    List<VariableServiceDetails> findByServiceId(Long serviceId);
}