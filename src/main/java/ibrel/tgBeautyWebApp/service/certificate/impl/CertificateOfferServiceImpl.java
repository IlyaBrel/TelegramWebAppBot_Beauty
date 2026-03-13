package ibrel.tgBeautyWebApp.service.certificate.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.certificate.CertificateOffer;
import ibrel.tgBeautyWebApp.model.certificate.CertificatePurchaseRequest.PurchaseStatus;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.repository.certificate.CertificateOfferRepository;
import ibrel.tgBeautyWebApp.repository.certificate.CertificatePurchaseRequestRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterServiceWorkRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.certificate.CertificateOfferService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateOfferServiceImpl implements CertificateOfferService {

    private final CertificateOfferRepository            offerRepository;
    private final CertificatePurchaseRequestRepository  purchaseRequestRepository;
    private final MasterRepository                      masterRepository;
    private final MasterServiceWorkRepository           serviceWorkRepository;
    private final UserRepository                        userRepository;

    // ── Создание ──────────────────────────────────────────────────────────────

    /**
     * Создать оффер для конкретного профиля мастера.
     * masterId — точный ID профиля, createdByTelegramId — кто создаёт (для логов).
     */
    @Override
    @Transactional
    public CertificateOffer create(Long masterId, CertificateOffer offer,
                                   Long createdByTelegramId) {
        Assert.notNull(masterId, "masterId must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Master not found id=" + masterId));

        return buildAndSave(offer, master, createdByTelegramId);
    }

    @Override
    @Transactional
    public CertificateOffer createForMaster(Long masterId, CertificateOffer offer,
                                            Long adminTelegramId) {
        Assert.notNull(masterId,        "masterId must not be null");
        Assert.notNull(adminTelegramId, "adminTelegramId must not be null");

        requireAdmin(adminTelegramId);

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        return buildAndSave(offer, master, adminTelegramId);
    }

    // ── Обновление ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CertificateOffer update(Long offerId, CertificateOffer updated,
                                   Long requesterTelegramId) {
        CertificateOffer existing = getById(offerId);
        requireOwnerOrAdmin(existing, requesterTelegramId);

        boolean hasPendingRequests = purchaseRequestRepository
                .findByOffer_IdOrderByCreatedAtDesc(offerId).stream()
                .anyMatch(r -> r.getStatus() == PurchaseStatus.PENDING);

        if (updated.getTitle()       != null) existing.setTitle(updated.getTitle());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getValidDays()   != null) existing.setValidDays(updated.getValidDays());

        // Цену и услуги нельзя менять пока есть активные заявки
        if (!hasPendingRequests) {
            if (updated.getPrice() != null) {
                if (updated.getPrice() <= 0)
                    throw new IllegalArgumentException("price must be > 0");
                existing.setPrice(updated.getPrice());
            }
            if (updated.getServices() != null && !updated.getServices().isEmpty()) {
                existing.setServices(resolveAndValidateServices(
                        updated.getServices(), existing.getMaster().getId()));
            }
        }

        existing.setUpdatedAt(OffsetDateTime.now());
        CertificateOffer saved = offerRepository.save(existing);
        log.info("Updated certificate offer id={}", offerId);
        return saved;
    }

    @Override
    @Transactional
    public CertificateOffer setActive(Long offerId, boolean active,
                                      Long requesterTelegramId) {
        CertificateOffer offer = getById(offerId);
        requireOwnerOrAdmin(offer, requesterTelegramId);
        offer.setActive(active);
        offer.setUpdatedAt(OffsetDateTime.now());
        log.info("Set offer id={} active={}", offerId, active);
        return offerRepository.save(offer);
    }

    @Override
    @Transactional
    public void delete(Long offerId, Long requesterTelegramId) {
        CertificateOffer offer = getById(offerId);
        requireOwnerOrAdmin(offer, requesterTelegramId);

        boolean hasPending = purchaseRequestRepository
                .findByOffer_IdOrderByCreatedAtDesc(offerId).stream()
                .anyMatch(r -> r.getStatus() == PurchaseStatus.PENDING);

        if (hasPending)
            throw new IllegalStateException("Cannot delete offer with pending purchase requests");

        offerRepository.deleteById(offerId);
        log.info("Deleted certificate offer id={}", offerId);
    }

    // ── Получение ─────────────────────────────────────────────────────────────

    @Override
    public CertificateOffer getById(Long offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "CertificateOffer not found id=" + offerId));
    }

    @Override
    public List<CertificateOffer> getByMaster(Long masterId) {
        return offerRepository.findByMaster_IdOrderByCreatedAtDesc(masterId);
    }

    @Override
    public List<CertificateOffer> getAvailableByMaster(Long masterId) {
        return offerRepository.findByMaster_IdAndActiveTrueOrderByCreatedAtDesc(masterId);
    }

    @Override
    public List<CertificateOffer> getAvailableByCity(Long cityId) {
        return offerRepository.findAvailableByCity(cityId);
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    private CertificateOffer buildAndSave(CertificateOffer offer, Master master,
                                          Long createdByTelegramId) {
        validateOffer(offer);
        List<MasterServiceWork> services = resolveAndValidateServices(
                offer.getServices(), master.getId());

        offer.setMaster(master);
        offer.setServices(services);
        offer.setActive(true);
        offer.setCreatedByTelegramId(createdByTelegramId);
        offer.setCreatedAt(OffsetDateTime.now());

        CertificateOffer saved = offerRepository.save(offer);
        log.info("Created certificate offer id={} for master id={}", saved.getId(), master.getId());
        return saved;
    }

    private void validateOffer(CertificateOffer offer) {
        Assert.notNull(offer, "offer must not be null");
        if (offer.getTitle() == null || offer.getTitle().isBlank())
            throw new IllegalArgumentException("offer title is required");
        if (offer.getPrice() == null || offer.getPrice() <= 0)
            throw new IllegalArgumentException("offer price must be > 0");
        if (offer.getServices() == null || offer.getServices().isEmpty())
            throw new IllegalArgumentException("offer must contain at least one service");
    }

    private List<MasterServiceWork> resolveAndValidateServices(
            List<MasterServiceWork> input, Long masterId) {
        List<Long> ids = input.stream().map(MasterServiceWork::getId).toList();
        List<MasterServiceWork> found = serviceWorkRepository.findAllById(ids);
        if (found.size() != ids.size())
            throw new EntityNotFoundException("Some services not found");
        found.forEach(s -> {
            if (!s.getMaster().getId().equals(masterId))
                throw new IllegalArgumentException(
                        "Service id=" + s.getId() + " does not belong to master id=" + masterId);
            if (!Boolean.TRUE.equals(s.getActive()))
                throw new IllegalArgumentException("Service id=" + s.getId() + " is not active");
        });
        return found;
    }

    /**
     * Доступ: владелец профиля (по telegramId) ИЛИ admin.
     * telegramId используется для идентификации запроса — все профили одного пользователя
     * имеют одинаковый telegramId, поэтому проверка корректна.
     */
    private void requireOwnerOrAdmin(CertificateOffer offer, Long requesterTelegramId) {
        boolean isOwner = offer.getMaster().getTelegramId().equals(requesterTelegramId);
        boolean isAdmin = userRepository.findByTelegramId(requesterTelegramId)
                .map(u -> UserRole.ADMIN.equals(u.getRole()))
                .orElse(false);
        if (!isOwner && !isAdmin)
            throw new IllegalArgumentException("Only the master or admin can modify this offer");
    }

    private void requireAdmin(Long telegramId) {
        var user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));
        if (!UserRole.ADMIN.equals(user.getRole()))
            throw new IllegalArgumentException("Only admin can perform this action");
    }
}