package ibrel.tgBeautyWebApp.service.promo;

import ibrel.tgBeautyWebApp.model.promo.MasterPromoCode;

import java.math.BigDecimal;
import java.util.List;

public interface PromoCodeService {

    /**
     * Валидация промокода: активен, не истёк, лимит не превышен.
     * Бросает исключение если невалиден.
     */
    MasterPromoCode validate(String code);

    /**
     * Рассчитать сумму скидки для данной базовой цены.
     */
    BigDecimal calculateDiscount(MasterPromoCode promoCode, BigDecimal basePrice);

    /**
     * Применить промокод: инкрементировать usedCount, деактивировать если лимит исчерпан.
     */
    void apply(MasterPromoCode promoCode);

    List<MasterPromoCode> getActiveCodes(Long masterId);

    MasterPromoCode create(Long masterId, String code, MasterPromoCode.PromoType type,
                           BigDecimal value, int usageLimit, java.time.LocalDate expiresAt);

    void deactivate(Long promoCodeId);
}
