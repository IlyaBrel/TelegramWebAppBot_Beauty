package ibrel.tgBeautyWebApp.repository.promo;

import ibrel.tgBeautyWebApp.model.promo.MasterServiceDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterServiceDiscountRepository extends JpaRepository<MasterServiceDiscount, Long> {

    List<MasterServiceDiscount> findByMaster_Id(Long masterId);
}
