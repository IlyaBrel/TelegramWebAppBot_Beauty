package ibrel.tgBeautyWebApp.repository.certificate;

import ibrel.tgBeautyWebApp.model.certificate.CertificateOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateOfferRepository extends JpaRepository<CertificateOffer, Long> {

    /** Все офферы мастера */
    List<CertificateOffer> findByMaster_IdOrderByCreatedAtDesc(Long masterId);

    /** Только активные офферы мастера */
    List<CertificateOffer> findByMaster_IdAndActiveTrueOrderByCreatedAtDesc(Long masterId);

    /** Активные офферы по городу мастера */
    @Query("""
        SELECT o FROM CertificateOffer o
        WHERE o.master.city.id = :cityId
          AND o.active = true
        ORDER BY o.master.cachedAvgRating DESC
    """)
    List<CertificateOffer> findAvailableByCity(@Param("cityId") Long cityId);

    boolean existsByIdAndMaster_TelegramId(Long offerId, Long masterTelegramId);
}