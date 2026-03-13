package ibrel.tgBeautyWebApp.repository.campaign;

import ibrel.tgBeautyWebApp.model.campaign.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Query("SELECT c FROM Campaign c WHERE c.active = true " +
           "AND c.startDate <= :today " +
           "AND (c.endDate IS NULL OR c.endDate >= :today)")
    List<Campaign> findActive(@Param("today") LocalDate today);

    List<Campaign> findByActiveTrue();
}
