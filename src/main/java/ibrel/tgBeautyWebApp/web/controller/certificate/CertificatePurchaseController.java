package ibrel.tgBeautyWebApp.web.controller.certificate;

import ibrel.tgBeautyWebApp.dto.certificate.CertificateDto;
import ibrel.tgBeautyWebApp.dto.certificate.CertificatePurchaseRequestDto;
import ibrel.tgBeautyWebApp.service.certificate.CertificatePurchaseService;
import ibrel.tgBeautyWebApp.web.mapper.certificate.CertificateMapper;
import ibrel.tgBeautyWebApp.web.mapper.certificate.CertificatePurchaseRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Флоу покупки сертификата:
 *   1. Покупатель → POST /api/certificate-purchases
 *   2. Мастер получает уведомление в Telegram
 *   3. Мастер → POST /api/certificate-purchases/{id}/approve   (или /reject)
 *   4. Покупатель → POST /api/certificate-purchases/{id}/cancel (пока PENDING)
 *
 * Для получения списка заявок мастера используется masterId (ID профиля),
 * НЕ masterTelegramId — у одного пользователя может быть несколько профилей.
 */
@RestController
@RequestMapping("/api/certificate-purchases")
@RequiredArgsConstructor
public class CertificatePurchaseController {

    private final CertificatePurchaseService       purchaseService;
    private final CertificatePurchaseRequestMapper requestMapper;
    private final CertificateMapper                certificateMapper;

    // ── Клиент ────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<CertificatePurchaseRequestDto> requestPurchase(
            @Valid @RequestBody CertificatePurchaseRequestDto dto,
            @RequestParam Long buyerTelegramId) {

        var saved = purchaseService.requestPurchase(
                buyerTelegramId, dto.getOfferId(), dto.getBuyerComment());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(requestMapper.toDto(saved));
    }

    @GetMapping("/my")
    public ResponseEntity<List<CertificatePurchaseRequestDto>> getMyRequests(
            @RequestParam Long buyerTelegramId) {
        return ResponseEntity.ok(
                purchaseService.getByBuyer(buyerTelegramId)
                        .stream().map(requestMapper::toDto).toList());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CertificatePurchaseRequestDto> cancel(
            @PathVariable Long id,
            @RequestParam Long buyerTelegramId) {
        return ResponseEntity.ok(requestMapper.toDto(
                purchaseService.cancelPurchase(id, buyerTelegramId)));
    }

    // ── Мастер — просмотр заявок ──────────────────────────────────────────────

    /**
     * Входящие PENDING заявки конкретного профиля мастера.
     * GET /api/certificate-purchases/incoming/pending?masterId=42
     *
     * masterId — ID профиля мастера, не telegramId.
     */
    @GetMapping("/incoming/pending")
    public ResponseEntity<List<CertificatePurchaseRequestDto>> getPending(
            @RequestParam Long masterId) {
        return ResponseEntity.ok(
                purchaseService.getPendingForMaster(masterId)
                        .stream().map(requestMapper::toDto).toList());
    }

    /**
     * Все заявки (история) конкретного профиля мастера.
     * GET /api/certificate-purchases/incoming/all?masterId=42
     */
    @GetMapping("/incoming/all")
    public ResponseEntity<List<CertificatePurchaseRequestDto>> getAllIncoming(
            @RequestParam Long masterId) {
        return ResponseEntity.ok(
                purchaseService.getAllForMaster(masterId)
                        .stream().map(requestMapper::toDto).toList());
    }

    // ── Мастер — одобрение / отклонение ──────────────────────────────────────

    /**
     * Одобрить заявку.
     * masterTelegramId — идентификатор запроса (кто нажал кнопку).
     * Используется для проверки что одобряет владелец профиля.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<CertificateDto> approve(
            @PathVariable Long id,
            @RequestParam Long masterTelegramId) {
        return ResponseEntity.ok(certificateMapper.toDto(
                purchaseService.approvePurchase(id, masterTelegramId)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<CertificatePurchaseRequestDto> reject(
            @PathVariable Long id,
            @RequestParam Long masterTelegramId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(requestMapper.toDto(
                purchaseService.rejectPurchase(id, masterTelegramId, reason)));
    }
}