package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.model.master.*;
import ibrel.tgBeautyWebApp.repository.catalog.AmenityRepository;
import ibrel.tgBeautyWebApp.repository.catalog.CityRepository;
import ibrel.tgBeautyWebApp.repository.catalog.SpecialtyRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterPersonalDataRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.master.MasterReviewService;
import ibrel.tgBeautyWebApp.service.master.MasterService;
import ibrel.tgBeautyWebApp.service.user.UserService;
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
public class MasterServiceImpl implements MasterService {

    private final MasterRepository           masterRepository;
    private final MasterPersonalDataRepository personalDataRepository;
    private final CityRepository             cityRepository;
    private final SpecialtyRepository        specialtyRepository;
    private final AmenityRepository          amenityRepository;
    private final UserRepository             userRepository;
    private final MasterReviewService        reviewService;
    private final UserService                userService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Master create(Master master, Long adminTelegramId) {
        Assert.notNull(master, "Master must not be null");
        Assert.notNull(master.getTelegramId(), "TelegramId must not be null");

        requireAdmin(adminTelegramId);

        userService.findByTelegramId(master.getTelegramId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found telegramId=" + master.getTelegramId()));

        // Проверяем уникальность profileName в рамках telegramId
        String profileName = master.getProfileName();
        if (profileName != null && !profileName.isBlank()) {
            if (masterRepository.existsByTelegramIdAndProfileName(master.getTelegramId(), profileName)) {
                throw new IllegalStateException(
                        "Master profile '" + profileName + "' already exists for telegramId=" + master.getTelegramId());
            }
        }

        if (master.getCity() != null && master.getCity().getId() != null) {
            City city = cityRepository.findById(master.getCity().getId())
                    .orElseThrow(() -> new EntityNotFoundException("City not found"));
            master.setCity(city);
        }

        master.setCreatedAt(OffsetDateTime.now());
        master.setUpdatedAt(OffsetDateTime.now());
        master.setActive(false);

        Master saved = masterRepository.save(master);
        log.info("Created master id={} telegramId={} profile='{}' by admin={}",
                saved.getId(), saved.getTelegramId(), saved.getProfileName(), adminTelegramId);
        return saved;
    }

    /**
     * Автоматически создаёт первый профиль мастера на основе TG-профиля пользователя.
     * Вызывается при смене роли на MASTER.
     * Если профиль уже существует — идемпотентно возвращает первый.
     */
    @Override
    @Transactional
    public Master createFromUser(UserTG user, Long adminTelegramId) {
        Assert.notNull(user, "User must not be null");

        // Идемпотентность: если уже есть хотя бы один профиль — возвращаем его
        List<Master> existing = masterRepository.findAllByTelegramId(user.getTelegramId());
        if (!existing.isEmpty()) {
            log.info("Master profile already exists for telegramId={}, skipping auto-create", user.getTelegramId());
            return existing.get(0);
        }

        // Создаём мастера
        Master master = Master.builder()
                .telegramId(user.getTelegramId())
                .profileName("Основной профиль")
                .active(false)
                .imageUrl(null)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Master saved = masterRepository.save(master);

        // Заполняем личные данные из TG-профиля
        MasterPersonalData pd = MasterPersonalData.builder()
                .master(saved)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
        personalDataRepository.save(pd);
        saved.setPersonalData(pd);

        log.info("Auto-created master id={} from user telegramId={} (role→MASTER)",
                saved.getId(), user.getTelegramId());
        return saved;
    }

    /**
     * Создаёт дополнительный профиль мастера для пользователя.
     * Требует, чтобы у пользователя уже была роль MASTER и хотя бы один профиль.
     */
    @Override
    @Transactional
    public Master createAdditionalProfile(Long telegramId, String profileName, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        Assert.hasText(profileName, "profileName must not be blank");

        UserTG user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        if (!UserRole.MASTER.equals(user.getRole()) && !UserRole.ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "User telegramId=" + telegramId + " does not have MASTER role");
        }

        if (masterRepository.existsByTelegramIdAndProfileName(telegramId, profileName)) {
            throw new IllegalStateException(
                    "Profile '" + profileName + "' already exists for telegramId=" + telegramId);
        }

        Master master = Master.builder()
                .telegramId(telegramId)
                .profileName(profileName)
                .active(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Master saved = masterRepository.save(master);

        // Копируем имя из TG-профиля
        MasterPersonalData pd = MasterPersonalData.builder()
                .master(saved)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
        personalDataRepository.save(pd);
        saved.setPersonalData(pd);

        log.info("Created additional master profile id={} '{}' for telegramId={} by admin={}",
                saved.getId(), profileName, telegramId, adminTelegramId);
        return saved;
    }

    @Override
    @Transactional
    public Master update(Long id, Master updated, Long requesterTelegramId) {
        Assert.notNull(id, "id must not be null");

        Master existing = getById(id);
        boolean isAdmin = isAdmin(requesterTelegramId);
        boolean isSelf  = existing.getTelegramId().equals(requesterTelegramId);

        if (!isAdmin && !isSelf) {
            throw new IllegalArgumentException("Only admin or the master themselves can update profile");
        }

        if (updated.getImageUrl()    != null) existing.setImageUrl(updated.getImageUrl());
        if (updated.getProfileName() != null && !updated.getProfileName().isBlank()) {
            // Проверяем что новое имя не занято у этого же пользователя
            String newName = updated.getProfileName();
            if (!newName.equals(existing.getProfileName()) &&
                    masterRepository.existsByTelegramIdAndProfileName(existing.getTelegramId(), newName)) {
                throw new IllegalStateException("Profile name '" + newName + "' already taken");
            }
            existing.setProfileName(newName);
        }

        if (isAdmin) {
            if (updated.getActive() != null) existing.setActive(updated.getActive());
            if (updated.getCity() != null && updated.getCity().getId() != null) {
                City city = cityRepository.findById(updated.getCity().getId())
                        .orElseThrow(() -> new EntityNotFoundException("City not found"));
                existing.setCity(city);
            }
        }

        existing.setUpdatedAt(OffsetDateTime.now());
        return masterRepository.save(existing);
    }

    @Override
    public Master getById(Long id) {
        Assert.notNull(id, "id must not be null");
        return masterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + id));
    }

    /** Первый профиль — для обратной совместимости (сертификаты и т.д.). */
    @Override
    public Master getByTelegramId(Long telegramId) {
        Assert.notNull(telegramId, "telegramId must not be null");
        return masterRepository.findFirstByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Master not found telegramId=" + telegramId));
    }

    @Override
    public List<Master> getAllByTelegramId(Long telegramId) {
        return masterRepository.findAllByTelegramId(telegramId);
    }

    @Override
    public List<Master> getAll() {
        return masterRepository.findAllByOrderByCachedAvgRatingDesc();
    }

    @Override
    public List<Master> getAllActive() {
        return masterRepository.findByActiveTrueOrderByCachedAvgRatingDesc();
    }

    @Override
    public List<Master> getByCity(Long cityId) {
        return masterRepository.findByActiveTrueAndCity_IdOrderByCachedAvgRatingDesc(cityId);
    }

    @Override
    public List<Master> getBySpecialty(Long specialtyId) {
        return masterRepository.findActiveBySpecialtyId(specialtyId);
    }

    @Override
    public List<Master> getByCityAndSpecialty(Long cityId, Long specialtyId) {
        return masterRepository.findActiveByCity_IdAndSpecialtyId(cityId, specialtyId);
    }

    @Override
    @Transactional
    public void delete(Long id, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        if (!masterRepository.existsById(id))
            throw new EntityNotFoundException("Master not found id=" + id);
        masterRepository.deleteById(id);
        log.info("Deleted master id={} by admin={}", id, adminTelegramId);
    }

    @Override
    @Transactional
    public Master activate(Long id) {
        Master m = getById(id);
        if (!Boolean.TRUE.equals(m.getActive())) {
            m.setActive(true);
            m.setUpdatedAt(OffsetDateTime.now());
            masterRepository.save(m);
        }
        return m;
    }

    @Override
    @Transactional
    public Master deactivate(Long id) {
        Master m = getById(id);
        if (!Boolean.FALSE.equals(m.getActive())) {
            m.setActive(false);
            m.setUpdatedAt(OffsetDateTime.now());
            masterRepository.save(m);
        }
        return m;
    }

    // ── Специализации и удобства ──────────────────────────────────────────────

    @Override
    @Transactional
    public Master addSpecialties(Long masterId, List<Long> specialtyIds, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        Master master = getById(masterId);

        List<Specialty> specialties = specialtyRepository.findAllById(specialtyIds);
        if (specialties.size() != specialtyIds.size())
            throw new EntityNotFoundException("Some specialties not found");

        master.setSpecialties(specialties);
        master.setUpdatedAt(OffsetDateTime.now());
        return masterRepository.save(master);
    }

    @Override
    @Transactional
    public Master addAmenities(Long masterId, List<Long> amenityIds, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        Master master = getById(masterId);

        List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
        if (amenities.size() != amenityIds.size())
            throw new EntityNotFoundException("Some amenities not found");

        master.setAmenities(amenities);
        master.setUpdatedAt(OffsetDateTime.now());
        return masterRepository.save(master);
    }

    // ── Рейтинг ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Master setBoostedRating(Long masterId, Double rating, Long adminTelegramId) {
        requireAdmin(adminTelegramId);

        if (rating < 0 || rating > 5)
            throw new IllegalArgumentException("Rating must be between 0 and 5");

        Master master = getById(masterId);
        master.setBoostedRating(rating);

        if (master.getReviewCount() == 0)
            master.setCachedAvgRating(rating);

        master.setUpdatedAt(OffsetDateTime.now());
        return masterRepository.save(master);
    }

    // ── Отзывы ────────────────────────────────────────────────────────────────

    @Override
    public MasterReview addManualReview(Long masterId, MasterReview review, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        review.setIsManual(true);
        review.setAuthorId(adminTelegramId.toString());
        return reviewService.addReview(masterId, review);
    }

    @Override
    public void deleteReview(Long reviewId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);
        reviewService.delete(reviewId);
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    private void requireAdmin(Long telegramId) {
        var user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));
        if (!UserRole.ADMIN.equals(user.getRole()))
            throw new IllegalArgumentException("Only admin can perform this action");
    }

    private boolean isAdmin(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .map(u -> UserRole.ADMIN.equals(u.getRole()))
                .orElse(false);
    }
}