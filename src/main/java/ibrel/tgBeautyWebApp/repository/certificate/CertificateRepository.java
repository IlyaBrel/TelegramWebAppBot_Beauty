package ibrel.tgBeautyWebApp.repository.certificate;

import ibrel.tgBeautyWebApp.model.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCode(String code);

    List<Certificate> findByMaster_IdOrderByCreatedAtDesc(Long masterId);

    List<Certificate> findByRecipientTelegramIdOrderByCreatedAtDesc(Long recipientTelegramId);

    List<Certificate> findByRecipientTelegramIdAndStatus(
            Long recipientTelegramId, Certificate.CertificateStatus status);

    boolean existsByCode(String code);
}