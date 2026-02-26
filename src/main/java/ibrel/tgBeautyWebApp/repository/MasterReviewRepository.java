package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.MasterReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterReviewRepository extends JpaRepository<MasterReview, Long> {
    List<MasterReview> findByMasterId(Long masterId);
}
