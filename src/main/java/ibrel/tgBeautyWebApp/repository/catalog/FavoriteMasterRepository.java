package ibrel.tgBeautyWebApp.repository.catalog;

import ibrel.tgBeautyWebApp.model.FavoriteMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteMasterRepository extends JpaRepository<FavoriteMaster, Long> {

    List<FavoriteMaster> findByUserTelegramId(Long userTelegramId);

    Optional<FavoriteMaster> findByUserTelegramIdAndMaster_Id(Long userTelegramId, Long masterId);

    boolean existsByUserTelegramIdAndMaster_Id(Long userTelegramId, Long masterId);

    void deleteByUserTelegramIdAndMaster_Id(Long userTelegramId, Long masterId);
}