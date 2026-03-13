package ibrel.tgBeautyWebApp.repository.promo;

import ibrel.tgBeautyWebApp.model.promo.MasterPromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasterPromoCodeRepository extends JpaRepository<MasterPromoCode, Long> {

    Optional<MasterPromoCode> findByCode(String code);

    List<MasterPromoCode> findByMaster_IdAndActiveTrue(Long masterId);
}
