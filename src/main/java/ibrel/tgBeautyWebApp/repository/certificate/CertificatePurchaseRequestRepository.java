package ibrel.tgBeautyWebApp.repository.certificate;

import ibrel.tgBeautyWebApp.model.certificate.CertificatePurchaseRequest;
import ibrel.tgBeautyWebApp.model.certificate.CertificatePurchaseRequest.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificatePurchaseRequestRepository
        extends JpaRepository<CertificatePurchaseRequest, Long> {

    /** Все заявки покупателя */
    List<CertificatePurchaseRequest> findByBuyerTelegramIdOrderByCreatedAtDesc(Long buyerTelegramId);

    /** Заявки покупателя по статусу */
    List<CertificatePurchaseRequest> findByBuyerTelegramIdAndStatusOrderByCreatedAtDesc(
            Long buyerTelegramId, PurchaseStatus status);

    /** Входящие заявки мастера (по всем его офферам) */
    @Query("""
        SELECT r FROM CertificatePurchaseRequest r
        WHERE r.offer.master.id = :masterId
        ORDER BY r.createdAt DESC
    """)
    List<CertificatePurchaseRequest> findByMasterId(@Param("masterId") Long masterId);

    /** Входящие PENDING заявки мастера */
    @Query("""
        SELECT r FROM CertificatePurchaseRequest r
        WHERE r.offer.master.id = :masterId
          AND r.status = 'PENDING'
        ORDER BY r.createdAt ASC
    """)
    List<CertificatePurchaseRequest> findPendingByMasterId(@Param("masterId") Long masterId);

    /** Заявки по конкретному офферу */
    List<CertificatePurchaseRequest> findByOffer_IdOrderByCreatedAtDesc(Long offerId);

    /** Уже есть активная заявка от покупателя на этот оффер */
    boolean existsByOffer_IdAndBuyerTelegramIdAndStatus(
            Long offerId, Long buyerTelegramId, PurchaseStatus status);
}