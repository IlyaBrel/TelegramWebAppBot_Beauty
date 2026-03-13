package ibrel.tgBeautyWebApp.web.controller.promo;

import ibrel.tgBeautyWebApp.model.promo.MasterPromoCode;
import ibrel.tgBeautyWebApp.model.promo.MasterPromoCode.PromoType;
import ibrel.tgBeautyWebApp.service.promo.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    /** Получить активные промокоды мастера */
    @GetMapping("/master/{masterId}")
    public ResponseEntity<List<MasterPromoCode>> getActiveCodes(@PathVariable Long masterId) {
        return ResponseEntity.ok(promoCodeService.getActiveCodes(masterId));
    }

    /**
     * Валидировать промокод.
     * Возвращает данные промокода или ошибку.
     */
    @GetMapping("/validate")
    public ResponseEntity<MasterPromoCode> validate(@RequestParam String code) {
        return ResponseEntity.ok(promoCodeService.validate(code));
    }

    /**
     * Рассчитать скидку по промокоду для указанной цены.
     */
    @GetMapping("/calculate-discount")
    public ResponseEntity<Map<String, BigDecimal>> calculateDiscount(
            @RequestParam String code,
            @RequestParam BigDecimal basePrice) {
        MasterPromoCode promo = promoCodeService.validate(code);
        BigDecimal discount = promoCodeService.calculateDiscount(promo, basePrice);
        return ResponseEntity.ok(Map.of(
                "basePrice", basePrice,
                "discountAmount", discount,
                "finalPrice", basePrice.subtract(discount)
        ));
    }

    /** Создать новый промокод для мастера */
    @PostMapping
    public ResponseEntity<MasterPromoCode> create(
            @RequestParam Long masterId,
            @RequestParam String code,
            @RequestParam PromoType type,
            @RequestParam BigDecimal value,
            @RequestParam int usageLimit,
            @RequestParam(required = false) LocalDate expiresAt) {
        MasterPromoCode created = promoCodeService.create(masterId, code, type, value, usageLimit, expiresAt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Деактивировать промокод */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        promoCodeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
