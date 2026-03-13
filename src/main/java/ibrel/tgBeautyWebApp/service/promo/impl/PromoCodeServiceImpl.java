package ibrel.tgBeautyWebApp.service.promo.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.promo.MasterPromoCode;
import ibrel.tgBeautyWebApp.model.promo.MasterPromoCode.PromoType;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.promo.MasterPromoCodeRepository;
import ibrel.tgBeautyWebApp.service.promo.PromoCodeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeServiceImpl implements PromoCodeService {

    private final MasterPromoCodeRepository promoRepo;
    private final MasterRepository masterRepo;

    @Override
    public MasterPromoCode validate(String code) {
        MasterPromoCode promo = promoRepo.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("PromoCode not found: " + code));

        if (!Boolean.TRUE.equals(promo.getActive())) {
            throw new IllegalStateException("PromoCode is inactive: " + code);
        }
        if (promo.getExpiresAt() != null && promo.getExpiresAt().isBefore(LocalDate.now())) {
            throw new IllegalStateException("PromoCode has expired: " + code);
        }
        if (promo.getUsedCount() >= promo.getUsageLimit()) {
            throw new IllegalStateException("PromoCode usage limit reached: " + code);
        }
        return promo;
    }

    @Override
    public BigDecimal calculateDiscount(MasterPromoCode promoCode, BigDecimal basePrice) {
        if (promoCode.getType() == PromoType.PERCENT) {
            return basePrice.multiply(promoCode.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // FIXED_AMOUNT — не больше самой цены
            return promoCode.getValue().min(basePrice);
        }
    }

    @Override
    @Transactional
    public void apply(MasterPromoCode promoCode) {
        promoCode.setUsedCount(promoCode.getUsedCount() + 1);
        if (promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            promoCode.setActive(false);
            log.info("PromoCode {} deactivated — limit reached", promoCode.getCode());
        }
        promoRepo.save(promoCode);
        log.info("PromoCode {} applied, usedCount={}", promoCode.getCode(), promoCode.getUsedCount());
    }

    @Override
    public List<MasterPromoCode> getActiveCodes(Long masterId) {
        return promoRepo.findByMaster_IdAndActiveTrue(masterId);
    }

    @Override
    @Transactional
    public MasterPromoCode create(Long masterId, String code, PromoType type,
                                  BigDecimal value, int usageLimit, LocalDate expiresAt) {
        Master master = masterRepo.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        MasterPromoCode promo = MasterPromoCode.builder()
                .master(master)
                .code(code.toUpperCase())
                .type(type)
                .value(value)
                .usageLimit(usageLimit)
                .expiresAt(expiresAt)
                .build();

        MasterPromoCode saved = promoRepo.save(promo);
        log.info("PromoCode created: {} for master id={}", saved.getCode(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public void deactivate(Long promoCodeId) {
        MasterPromoCode promo = promoRepo.findById(promoCodeId)
                .orElseThrow(() -> new EntityNotFoundException("PromoCode not found id=" + promoCodeId));
        promo.setActive(false);
        promoRepo.save(promo);
        log.info("PromoCode id={} deactivated", promoCodeId);
    }
}
