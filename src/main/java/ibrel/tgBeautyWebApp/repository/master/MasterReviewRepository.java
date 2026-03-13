package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.MasterReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterReviewRepository extends JpaRepository<MasterReview, Long> {

    /**
     * Оригинальный вариант (без подчёркивания) — оставлен для обратной совместимости.
     */
    List<MasterReview> findByMasterIdOrderByCreatedAtDesc(Long masterId);

    /**
     * Вариант с явным разделителем пути — используется в MasterReviewServiceImpl.getByMaster().
     */
    List<MasterReview> findByMaster_IdOrderByCreatedAtDesc(Long masterId);

    boolean existsByAppointment_Id(Long appointmentId);

    boolean existsByMaster_IdAndAuthorId(Long masterId, String authorId);

    /**
     * Количество отзывов мастера — используется в MasterReviewServiceImpl.recalcRating().
     */
    long countByMaster_Id(Long masterId);

    @Query("select avg(r.rating) from MasterReview r where r.master.id = :masterId")
    Double findAverageRatingByMasterId(@Param("masterId") Long masterId);
}