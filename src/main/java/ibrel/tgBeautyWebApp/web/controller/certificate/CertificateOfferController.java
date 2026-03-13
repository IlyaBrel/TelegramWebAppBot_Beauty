package ibrel.tgBeautyWebApp.web.controller.certificate;

import ibrel.tgBeautyWebApp.dto.certificate.CertificateOfferDto;
import ibrel.tgBeautyWebApp.model.certificate.CertificateOffer;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.service.certificate.CertificateOfferService;
import ibrel.tgBeautyWebApp.web.mapper.certificate.CertificateOfferMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificate-offers")
@RequiredArgsConstructor
public class CertificateOfferController {

    private final CertificateOfferService offerService;
    private final CertificateOfferMapper  mapper;

    // ── Публичные (клиент) ────────────────────────────────────────────────────

    /**
     * Офферы конкретного профиля мастера.
     * GET /api/certificate-offers/master/{masterId}
     */
    @GetMapping("/master/{masterId}")
    public ResponseEntity<List<CertificateOfferDto>> getAvailableByMaster(
            @PathVariable Long masterId) {
        return ResponseEntity.ok(
                offerService.getAvailableByMaster(masterId).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<CertificateOfferDto>> getAvailableByCity(
            @PathVariable Long cityId) {
        return ResponseEntity.ok(
                offerService.getAvailableByCity(cityId).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateOfferDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(offerService.getById(id)));
    }

    // ── Мастер — личный кабинет ───────────────────────────────────────────────

    /**
     * Все офферы конкретного профиля мастера (включая неактивные).
     * Используем masterId — не telegramId — потому что у пользователя может
     * быть несколько профилей мастера.
     *
     * GET /api/certificate-offers/my?masterId=42
     */
    @GetMapping("/my")
    public ResponseEntity<List<CertificateOfferDto>> getMy(
            @RequestParam Long masterId) {
        return ResponseEntity.ok(
                offerService.getByMaster(masterId).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Создать оффер для конкретного профиля мастера.
     * masterId — ID профиля, masterTelegramId — кто запрашивает (для доступа).
     *
     * POST /api/certificate-offers?masterId=42&masterTelegramId=123456
     */
    @PostMapping
    public ResponseEntity<CertificateOfferDto> create(
            @Valid @RequestBody CertificateOfferDto dto,
            @RequestParam Long masterId,
            @RequestParam Long masterTelegramId) {

        CertificateOffer entity = toEntityWithServiceRefs(dto);
        CertificateOffer created = offerService.create(masterId, entity, masterTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    /**
     * Admin создаёт оффер для конкретного профиля мастера.
     * POST /api/certificate-offers/for-master/{masterId}?adminTelegramId=...
     */
    @PostMapping("/for-master/{masterId}")
    public ResponseEntity<CertificateOfferDto> createForMaster(
            @PathVariable Long masterId,
            @Valid @RequestBody CertificateOfferDto dto,
            @RequestParam Long adminTelegramId) {

        CertificateOffer entity = toEntityWithServiceRefs(dto);
        CertificateOffer created = offerService.createForMaster(masterId, entity, adminTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    // ── Управление (мастер или admin) ─────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<CertificateOfferDto> update(
            @PathVariable Long id,
            @RequestBody CertificateOfferDto dto,
            @RequestParam Long requesterTelegramId) {
        CertificateOffer entity = toEntityWithServiceRefs(dto);
        return ResponseEntity.ok(
                mapper.toDto(offerService.update(id, entity, requesterTelegramId)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<CertificateOfferDto> setActive(
            @PathVariable Long id,
            @RequestParam boolean active,
            @RequestParam Long requesterTelegramId) {
        return ResponseEntity.ok(
                mapper.toDto(offerService.setActive(id, active, requesterTelegramId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Long requesterTelegramId) {
        offerService.delete(id, requesterTelegramId);
        return ResponseEntity.noContent().build();
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    private CertificateOffer toEntityWithServiceRefs(CertificateOfferDto dto) {
        CertificateOffer entity = mapper.toEntity(dto);
        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()) {
            List<MasterServiceWork> stubs = dto.getServiceIds().stream()
                    .map(id -> {
                        MasterServiceWork s = new MasterServiceWork();
                        s.setId(id);
                        return s;
                    })
                    .collect(Collectors.toList());
            entity.setServices(stubs);
        }
        return entity;
    }
}