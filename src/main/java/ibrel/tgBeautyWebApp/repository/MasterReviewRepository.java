package ibrel.tgBeautyWebApp.repository;

import ibrel.tgBeautyWebApp.model.master.MasterReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterReviewRepository extends JpaRepository<MasterReview, Long> {
    List<MasterReview> findByMasterIdOrderByCreatedAtDesc(Long masterId);

    boolean existsByAppointment_Id(Long appointmentId);

    boolean existsByMaster_IdAndAuthorId(Long masterId, String authorId);

    @Query("select avg(r.rating) from MasterReview r where r.master.id = :masterId")
    Double findAverageRatingByMasterId(@Param("masterId") Long masterId);
}
