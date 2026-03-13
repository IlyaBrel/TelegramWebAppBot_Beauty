package ibrel.tgBeautyWebApp.repository.catalog;

import ibrel.tgBeautyWebApp.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /** Активные и не истёкшие акции для главной страницы */
    @Query("select p from Promotion p " +
            "where p.active = true " +
            "and (p.expiresAt is null or p.expiresAt > :now) " +
            "order by p.sortOrder asc, p.createdAt desc")
    List<Promotion> findActivePromotions(@Param("now") OffsetDateTime now);

    /** Все акции для панели администратора */
    List<Promotion> findAllByOrderBySortOrderAscCreatedAtDesc();
}