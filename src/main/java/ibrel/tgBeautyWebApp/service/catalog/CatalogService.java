package ibrel.tgBeautyWebApp.service.catalog;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Amenity;
import ibrel.tgBeautyWebApp.model.master.City;
import ibrel.tgBeautyWebApp.model.master.Specialty;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.model.Promotion;
import ibrel.tgBeautyWebApp.repository.catalog.AmenityRepository;
import ibrel.tgBeautyWebApp.repository.catalog.CityRepository;
import ibrel.tgBeautyWebApp.repository.catalog.PromotionRepository;
import ibrel.tgBeautyWebApp.repository.catalog.SpecialtyRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Сервис для справочников: Город, Специализация, Удобство, Акция.
 * Все операции записи — только для ADMIN.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final CityRepository cityRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AmenityRepository amenityRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;

    // ── Города ───────────────────────────────────────────────────────────────

    public List<City> getCities() {
        return cityRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public City createCity(City city, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        if (cityRepository.existsByNameIgnoreCase(city.getName()))
            throw new IllegalArgumentException("City already exists: " + city.getName());
        City saved = cityRepository.save(city);
        log.info("Created city id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public City updateCity(Long cityId, City updated, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        City existing = cityRepository.findById(cityId)
                .orElseThrow(() -> new EntityNotFoundException("City not found id=" + cityId));
        if (updated.getName()   != null) existing.setName(updated.getName());
        if (updated.getRegion() != null) existing.setRegion(updated.getRegion());
        if (updated.getActive() != null) existing.setActive(updated.getActive());
        return cityRepository.save(existing);
    }

    @Transactional
    public void deleteCity(Long cityId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        cityRepository.deleteById(cityId);
    }

    // ── Специализации ─────────────────────────────────────────────────────────

    public List<Specialty> getSpecialties() {
        return specialtyRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public Specialty createSpecialty(Specialty specialty, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        if (specialtyRepository.existsByNameIgnoreCase(specialty.getName()))
            throw new IllegalArgumentException("Specialty already exists: " + specialty.getName());
        Specialty saved = specialtyRepository.save(specialty);
        log.info("Created specialty id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Specialty updateSpecialty(Long specialtyId, Specialty updated, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        Specialty existing = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new EntityNotFoundException("Specialty not found id=" + specialtyId));
        if (updated.getName()        != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getIcon()        != null) existing.setIcon(updated.getIcon());
        if (updated.getActive()      != null) existing.setActive(updated.getActive());
        return specialtyRepository.save(existing);
    }

    @Transactional
    public void deleteSpecialty(Long specialtyId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        specialtyRepository.deleteById(specialtyId);
    }

    // ── Удобства ──────────────────────────────────────────────────────────────

    public List<Amenity> getAmenities() {
        return amenityRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public Amenity createAmenity(Amenity amenity, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        if (amenityRepository.existsByNameIgnoreCase(amenity.getName()))
            throw new IllegalArgumentException("Amenity already exists: " + amenity.getName());
        Amenity saved = amenityRepository.save(amenity);
        log.info("Created amenity id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Amenity updateAmenity(Long amenityId, Amenity updated, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        Amenity existing = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new EntityNotFoundException("Amenity not found id=" + amenityId));
        if (updated.getName()        != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getIcon()        != null) existing.setIcon(updated.getIcon());
        if (updated.getActive()      != null) existing.setActive(updated.getActive());
        return amenityRepository.save(existing);
    }

    @Transactional
    public void deleteAmenity(Long amenityId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        amenityRepository.deleteById(amenityId);
    }

    // ── Акции / промо ─────────────────────────────────────────────────────────

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(OffsetDateTime.now());
    }

    public List<Promotion> getAllPromotions(Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        return promotionRepository.findAllByOrderBySortOrderAscCreatedAtDesc();
    }

    @Transactional
    public Promotion createPromotion(Promotion promotion, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        promotion.setCreatedAt(OffsetDateTime.now());
        Promotion saved = promotionRepository.save(promotion);
        log.info("Created promotion id={}", saved.getId());
        return saved;
    }

    @Transactional
    public Promotion updatePromotion(Long promoId, Promotion updated, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        Promotion existing = promotionRepository.findById(promoId)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found id=" + promoId));
        if (updated.getTitle()       != null) existing.setTitle(updated.getTitle());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getImageUrl()    != null) existing.setImageUrl(updated.getImageUrl());
        if (updated.getActionUrl()   != null) existing.setActionUrl(updated.getActionUrl());
        if (updated.getType()        != null) existing.setType(updated.getType());
        if (updated.getSortOrder()   != null) existing.setSortOrder(updated.getSortOrder());
        if (updated.getActive()      != null) existing.setActive(updated.getActive());
        if (updated.getExpiresAt()   != null) existing.setExpiresAt(updated.getExpiresAt());
        return promotionRepository.save(existing);
    }

    @Transactional
    public void deletePromotion(Long promoId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        promotionRepository.deleteById(promoId);
    }

    // ── Вспомогательное ───────────────────────────────────────────────────────

    private void requireAdmin(Long telegramId) {
        var user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));
        if (!UserRole.ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("Only admin can perform this action");
        }
    }
}