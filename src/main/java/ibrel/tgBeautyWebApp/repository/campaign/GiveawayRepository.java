package ibrel.tgBeautyWebApp.repository.campaign;

import ibrel.tgBeautyWebApp.model.campaign.Giveaway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GiveawayRepository extends JpaRepository<Giveaway, Long> {

    @Query("SELECT g FROM Giveaway g WHERE g.active = true " +
           "AND g.startDate <= :today " +
           "AND (g.endDate IS NULL OR g.endDate >= :today)")
    List<Giveaway> findActive(@Param("today") LocalDate today);
}
