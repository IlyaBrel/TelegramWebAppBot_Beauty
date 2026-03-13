package ibrel.tgBeautyWebApp.web.controller.certificate;

import ibrel.tgBeautyWebApp.dto.certificate.CertificateDto;
import ibrel.tgBeautyWebApp.service.certificate.CertificateService;
import ibrel.tgBeautyWebApp.web.mapper.certificate.CertificateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateMapper  mapper;

    // ── Публичные ─────────────────────────────────────────────────────────────

    @GetMapping("/{code}")
    public ResponseEntity<CertificateDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(mapper.toDto(certificateService.findByCode(code)));
    }

    // ── Клиент ────────────────────────────────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<List<CertificateDto>> getMy(@RequestParam Long telegramId) {
        return ResponseEntity.ok(
                certificateService.getByRecipient(telegramId).stream().map(mapper::toDto).toList());
    }

    @GetMapping("/my/active")
    public ResponseEntity<List<CertificateDto>> getMyActive(@RequestParam Long telegramId) {
        return ResponseEntity.ok(
                certificateService.getActiveByRecipient(telegramId).stream().map(mapper::toDto).toList());
    }

    // ── Мастер ────────────────────────────────────────────────────────────────

    /**
     * Все сертификаты конкретного профиля мастера.
     * GET /api/certificates/by-master/{masterId}
     */
    @GetMapping("/by-master/{masterId}")
    public ResponseEntity<List<CertificateDto>> getByMaster(@PathVariable Long masterId) {
        return ResponseEntity.ok(
                certificateService.getByMaster(masterId).stream().map(mapper::toDto).toList());
    }

    /**
     * Мастер вручную дарит сертификат пользователю.
     * masterId — ID конкретного профиля мастера (не telegramId!).
     *
     * POST /api/certificates/issue
     * {
     *   "masterId": 42,
     *   "recipientTelegramId": 123456,
     *   "serviceIds": [1, 2],
     *   "validDays": 30
     * }
     */
    @PostMapping("/issue")
    public ResponseEntity<CertificateDto> issue(@RequestBody IssueRequest req) {
        return ResponseEntity.ok(mapper.toDto(
                certificateService.issue(
                        req.masterId(),
                        req.recipientTelegramId(),
                        req.serviceIds(),
                        req.validDays()
                )
        ));
    }

    /** Отменить сертификат (только выдавший мастер или admin) */
    @PostMapping("/{code}/cancel")
    public ResponseEntity<CertificateDto> cancel(@PathVariable String code,
                                                 @RequestParam Long requesterTelegramId) {
        return ResponseEntity.ok(mapper.toDto(
                certificateService.cancel(code, requesterTelegramId)));
    }

    // ── DTO ───────────────────────────────────────────────────────────────────

    /**
     * masterId — ID профиля мастера.
     * Заменяет masterTelegramId — у одного пользователя может быть несколько профилей.
     */
    public record IssueRequest(
            Long masterId,
            Long recipientTelegramId,
            List<Long> serviceIds,
            Integer validDays
    ) {}
}