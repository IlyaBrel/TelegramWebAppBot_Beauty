package ibrel.tgBeautyWebApp.service.certificate.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.certificate.Certificate;
import ibrel.tgBeautyWebApp.model.certificate.CertificateOffer;
import ibrel.tgBeautyWebApp.model.certificate.CertificatePurchaseRequest;
import ibrel.tgBeautyWebApp.model.certificate.CertificatePurchaseRequest.PurchaseStatus;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.repository.certificate.CertificateOfferRepository;
import ibrel.tgBeautyWebApp.repository.certificate.CertificatePurchaseRequestRepository;
import ibrel.tgBeautyWebApp.repository.certificate.CertificateRepository;
import ibrel.tgBeautyWebApp.service.certificate.CertificatePurchaseService;
import ibrel.tgBeautyWebApp.service.tg.TelegramNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificatePurchaseServiceImpl implements CertificatePurchaseService {

    private final CertificatePurchaseRequestRepository purchaseRepo;
    private final CertificateOfferRepository           offerRepo;
    private final CertificateRepository                certificateRepo;
    private final TelegramNotificationService          telegramService;

    // ── Создание заявки ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public CertificatePurchaseRequest requestPurchase(Long buyerTelegramId,
                                                      Long offerId,
                                                      String comment) {
        Assert.notNull(buyerTelegramId, "buyerTelegramId must not be null");
        Assert.notNull(offerId, "offerId must not be null");

        CertificateOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "CertificateOffer not found id=" + offerId));

        if (!offer.isAvailable())
            throw new IllegalStateException("This offer is not available");

        if (purchaseRepo.existsByOffer_IdAndBuyerTelegramIdAndStatus(
                offerId, buyerTelegramId, PurchaseStatus.PENDING))
            throw new IllegalStateException("You already have a pending request for this offer");

        CertificatePurchaseRequest request = CertificatePurchaseRequest.builder()
                .offer(offer)
                .buyerTelegramId(buyerTelegramId)
                .buyerComment(comment)
                .status(PurchaseStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        CertificatePurchaseRequest saved = purchaseRepo.save(request);
        notifyMasterNewPurchaseRequest(offer, saved);

        log.info("Purchase request id={} offerId={} buyer={}", saved.getId(), offerId, buyerTelegramId);
        return saved;
    }

    // ── Одобрение мастером ────────────────────────────────────────────────────

    /**
     * masterTelegramId здесь — идентификатор запроса (кто нажал «Одобрить»).
     * Проверяем что telegramId совпадает с владельцем профиля.
     * У одного пользователя может быть несколько профилей, но все принадлежат
     * одному telegramId — поэтому проверка через telegramId корректна.
     */
    @Override
    @Transactional
    public Certificate approvePurchase(Long requestId, Long masterTelegramId) {
        CertificatePurchaseRequest request = getRequest(requestId);
        requireMasterOwner(request, masterTelegramId);

        if (request.getStatus() != PurchaseStatus.PENDING)
            throw new IllegalStateException("Only PENDING requests can be approved");

        CertificateOffer offer = request.getOffer();
        Master master = offer.getMaster();

        String code = generateUniqueCode();
        OffsetDateTime now = OffsetDateTime.now();

        Certificate certificate = Certificate.builder()
                .code(code)
                .master(master)            // ← конкретный профиль из оффера
                .recipientTelegramId(request.getBuyerTelegramId())
                .services(offer.getServices())
                .status(Certificate.CertificateStatus.ACTIVE)
                .createdAt(now)
                .expiresAt(offer.getValidDays() != null ? now.plusDays(offer.getValidDays()) : null)
                .build();

        Certificate savedCert = certificateRepo.save(certificate);

        request.setStatus(PurchaseStatus.APPROVED);
        request.setCertificate(savedCert);
        request.setUpdatedAt(now);
        purchaseRepo.save(request);

        // Уведомления
        String masterName = getMasterDisplayName(master);
        String serviceNames = formatServiceNames(offer.getServices());

        try {
            telegramService.notifyCertificateIssued(
                    request.getBuyerTelegramId(), masterName, code, serviceNames);
        } catch (Exception e) {
            log.warn("Could not notify buyer: {}", e.getMessage());
        }

        try {
            telegramService.sendMessage(
                    master.getTelegramId(),
                    String.format("✅ Сертификат <code>%s</code> выдан покупателю #%d",
                            code, request.getBuyerTelegramId()));
        } catch (Exception e) {
            log.warn("Could not notify master: {}", e.getMessage());
        }

        log.info("Approved purchase id={}, issued certificate code={}", requestId, code);
        return savedCert;
    }

    // ── Отклонение мастером ───────────────────────────────────────────────────

    @Override
    @Transactional
    public CertificatePurchaseRequest rejectPurchase(Long requestId,
                                                     Long masterTelegramId,
                                                     String reason) {
        CertificatePurchaseRequest request = getRequest(requestId);
        requireMasterOwner(request, masterTelegramId);

        if (request.getStatus() != PurchaseStatus.PENDING)
            throw new IllegalStateException("Only PENDING requests can be rejected");

        request.setStatus(PurchaseStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setUpdatedAt(OffsetDateTime.now());
        CertificatePurchaseRequest saved = purchaseRepo.save(request);

        String masterName = getMasterDisplayName(request.getOffer().getMaster());
        try {
            telegramService.sendMessage(
                    request.getBuyerTelegramId(),
                    String.format("❌ <b>Заявка на сертификат отклонена</b>\n\n" +
                                    "👩‍🎨 Мастер: %s\n" +
                                    "🎁 Предложение: %s\n" +
                                    "💬 Причина: %s",
                            masterName,
                            request.getOffer().getTitle(),
                            reason != null ? reason : "не указана"));
        } catch (Exception e) {
            log.warn("Could not notify buyer about rejection: {}", e.getMessage());
        }

        log.info("Rejected purchase request id={}", requestId);
        return saved;
    }

    // ── Отмена пользователем ──────────────────────────────────────────────────

    @Override
    @Transactional
    public CertificatePurchaseRequest cancelPurchase(Long requestId, Long buyerTelegramId) {
        CertificatePurchaseRequest request = getRequest(requestId);

        if (!request.getBuyerTelegramId().equals(buyerTelegramId))
            throw new IllegalArgumentException("You can only cancel your own requests");
        if (request.getStatus() != PurchaseStatus.PENDING)
            throw new IllegalStateException("Only PENDING requests can be cancelled");

        request.setStatus(PurchaseStatus.CANCELLED);
        request.setUpdatedAt(OffsetDateTime.now());
        CertificatePurchaseRequest saved = purchaseRepo.save(request);

        try {
            telegramService.sendMessage(
                    request.getOffer().getMaster().getTelegramId(),
                    String.format("ℹ️ Покупатель #%d отменил заявку на сертификат «%s»",
                            buyerTelegramId, request.getOffer().getTitle()));
        } catch (Exception e) {
            log.warn("Could not notify master: {}", e.getMessage());
        }

        log.info("Cancelled purchase request id={} by buyer={}", requestId, buyerTelegramId);
        return saved;
    }

    // ── Получение ─────────────────────────────────────────────────────────────

    @Override
    public List<CertificatePurchaseRequest> getByBuyer(Long buyerTelegramId) {
        return purchaseRepo.findByBuyerTelegramIdOrderByCreatedAtDesc(buyerTelegramId);
    }

    /**
     * PENDING заявки конкретного профиля мастера.
     * masterId — прямой ID профиля, без lookup по telegramId.
     */
    @Override
    public List<CertificatePurchaseRequest> getPendingForMaster(Long masterId) {
        return purchaseRepo.findPendingByMasterId(masterId);
    }

    /**
     * Все заявки конкретного профиля мастера.
     * masterId — прямой ID профиля.
     */
    @Override
    public List<CertificatePurchaseRequest> getAllForMaster(Long masterId) {
        return purchaseRepo.findByMasterId(masterId);
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    private CertificatePurchaseRequest getRequest(Long requestId) {
        return purchaseRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "PurchaseRequest not found id=" + requestId));
    }

    /**
     * Проверка: requesterTelegramId является владельцем профиля, к которому относится оффер.
     * Работает корректно при нескольких профилях — все профили одного пользователя
     * имеют одинаковый telegramId.
     */
    private void requireMasterOwner(CertificatePurchaseRequest request, Long requesterTelegramId) {
        if (!request.getOffer().getMaster().getTelegramId().equals(requesterTelegramId))
            throw new IllegalArgumentException("Request does not belong to your offers");
    }

    private void notifyMasterNewPurchaseRequest(CertificateOffer offer,
                                                CertificatePurchaseRequest request) {
        String text = String.format(
                "🛒 <b>Новая заявка на покупку сертификата!</b>\n\n" +
                        "🎁 Предложение: %s\n" +
                        "💰 Цена: %.0f ₽\n" +
                        "👤 Покупатель: #%d\n" +
                        "%s",
                offer.getTitle(),
                offer.getPrice(),
                request.getBuyerTelegramId(),
                request.getBuyerComment() != null
                        ? "💬 Комментарий: " + request.getBuyerComment() : "");

        InlineKeyboardButton approve = InlineKeyboardButton.builder()
                .text("✅ Одобрить").callbackData("cert_approve:" + request.getId()).build();
        InlineKeyboardButton reject = InlineKeyboardButton.builder()
                .text("❌ Отклонить").callbackData("cert_reject:" + request.getId()).build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(approve, reject)))
                .build();

        try {
            telegramService.sendMessageWithButtons(
                    offer.getMaster().getTelegramId(), text, keyboard);
        } catch (Exception e) {
            log.warn("Could not notify master: {}", e.getMessage());
        }
    }

    private String getMasterDisplayName(Master master) {
        String name = master.getPersonalData() != null
                ? master.getPersonalData().getFirstName() : "Мастер";
        if (master.getProfileName() != null && !master.getProfileName().isBlank()) {
            name += " (" + master.getProfileName() + ")";
        }
        return name;
    }

    private String formatServiceNames(List<MasterServiceWork> services) {
        return services.stream().map(MasterServiceWork::getName).collect(Collectors.joining(", "));
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
            if (++attempts > 10) throw new IllegalStateException("Could not generate unique code");
        } while (certificateRepo.existsByCode(code));
        return code;
    }
}