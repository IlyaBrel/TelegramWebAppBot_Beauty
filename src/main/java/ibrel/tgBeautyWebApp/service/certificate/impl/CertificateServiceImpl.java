package ibrel.tgBeautyWebApp.service.certificate.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.certificate.Certificate;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.repository.certificate.CertificateRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterServiceWorkRepository;
import ibrel.tgBeautyWebApp.service.certificate.CertificateService;
import ibrel.tgBeautyWebApp.service.tg.TelegramNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository        certificateRepository;
    private final MasterRepository             masterRepository;
    private final MasterServiceWorkRepository  serviceWorkRepository;
    private final TelegramNotificationService  telegramService;

    /**
     * Мастер выдаёт сертификат.
     * masterId — ID конкретного профиля мастера (не telegramId!).
     */
    @Override
    @Transactional
    public Certificate issue(Long masterId, Long recipientTelegramId,
                             List<Long> serviceIds, Integer validDays) {

        // Ищем по ID профиля — один конкретный профиль, без путаницы
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Master not found id=" + masterId));

        List<MasterServiceWork> services = serviceWorkRepository.findAllById(serviceIds);
        if (services.size() != serviceIds.size())
            throw new EntityNotFoundException("Some services not found");

        // Услуги должны принадлежать именно этому профилю мастера
        services.forEach(s -> {
            if (!s.getMaster().getId().equals(master.getId()))
                throw new IllegalArgumentException(
                        "Service id=" + s.getId() + " does not belong to master profile id=" + masterId);
        });

        String code = generateUniqueCode();
        OffsetDateTime now = OffsetDateTime.now();

        Certificate cert = Certificate.builder()
                .code(code)
                .master(master)
                .recipientTelegramId(recipientTelegramId)
                .services(services)
                .status(Certificate.CertificateStatus.ACTIVE)
                .createdAt(now)
                .expiresAt(validDays != null ? now.plusDays(validDays) : null)
                .build();

        Certificate saved = certificateRepository.save(cert);

        String masterName = master.getPersonalData() != null
                ? master.getPersonalData().getFirstName() : "Мастер";
        // Добавляем profileName чтобы получатель знал от какого именно профиля
        if (master.getProfileName() != null && !master.getProfileName().isBlank()) {
            masterName += " (" + master.getProfileName() + ")";
        }
        String serviceNames = services.stream()
                .map(MasterServiceWork::getName)
                .collect(Collectors.joining(", "));

        try {
            telegramService.notifyCertificateIssued(
                    recipientTelegramId, masterName, code, serviceNames);
        } catch (Exception e) {
            log.warn("Could not notify recipient telegramId={}: {}", recipientTelegramId, e.getMessage());
        }

        log.info("Issued certificate code={} masterId={} recipient={}", code, masterId, recipientTelegramId);
        return saved;
    }

    @Override
    public Certificate findByCode(String code) {
        return certificateRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Certificate not found code=" + code));
    }

    @Override
    public List<Certificate> getByRecipient(Long recipientTelegramId) {
        return certificateRepository.findByRecipientTelegramIdOrderByCreatedAtDesc(recipientTelegramId);
    }

    @Override
    public List<Certificate> getActiveByRecipient(Long recipientTelegramId) {
        return certificateRepository.findByRecipientTelegramIdAndStatus(
                recipientTelegramId, Certificate.CertificateStatus.ACTIVE);
    }

    @Override
    public List<Certificate> getByMaster(Long masterId) {
        return certificateRepository.findByMaster_IdOrderByCreatedAtDesc(masterId);
    }

    /**
     * Отмена сертификата.
     * requesterTelegramId используется только для проверки доступа —
     * владелец сертификата определяется по master.telegramId или по роли ADMIN.
     */
    @Override
    @Transactional
    public Certificate cancel(String code, Long requesterTelegramId) {
        Certificate cert = findByCode(code);

        // Проверяем: requester — владелец профиля мастера (по telegramId) или admin
        boolean isOwner = cert.getMaster().getTelegramId().equals(requesterTelegramId);
        if (!isOwner) {
            throw new IllegalArgumentException("Only the issuing master can cancel the certificate");
        }
        if (cert.getStatus() != Certificate.CertificateStatus.ACTIVE) {
            throw new IllegalStateException("Certificate is not active");
        }

        cert.setStatus(Certificate.CertificateStatus.CANCELLED);
        Certificate saved = certificateRepository.save(cert);
        log.info("Cancelled certificate code={} by requester={}", code, requesterTelegramId);
        return saved;
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
            if (++attempts > 10) throw new IllegalStateException("Could not generate unique certificate code");
        } while (certificateRepository.existsByCode(code));
        return code;
    }
}