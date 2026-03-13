package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterReviewRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.master.MasterReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterReviewServiceImpl implements MasterReviewService {

    private final MasterRepository       masterRepository;
    private final MasterReviewRepository reviewRepository;
    private final AppointmentRepository  appointmentRepository;
    private final UserRepository         userRepository;

    // ── Реальный отзыв от клиента ─────────────────────────────────────────────

    @Override
    @Transactional
    public MasterReview addReview(Long masterId, MasterReview review) {
        Assert.notNull(masterId,          "masterId must not be null");
        Assert.notNull(review,            "review must not be null");
        Assert.notNull(review.getAuthorId(), "authorId must not be null");
        Assert.notNull(review.getRating(), "rating must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Master not found id=" + masterId));

        // Ручной отзыв (isManual=true) — пропускаем все дублирование-проверки
        if (!Boolean.TRUE.equals(review.getIsManual())) {
            if (review.getAppointment() != null && review.getAppointment().getId() != null) {
                Long appointmentId = review.getAppointment().getId();
                if (!appointmentRepository.existsById(appointmentId)) {
                    throw new EntityNotFoundException("Appointment not found id=" + appointmentId);
                }
                appointmentRepository.findById(appointmentId).ifPresent(a -> {
                    if (a.getMaster() == null || !Objects.equals(a.getMaster().getId(), masterId)) {
                        throw new IllegalArgumentException(
                                "Appointment does not belong to the specified master");
                    }
                });
                if (reviewRepository.existsByAppointment_Id(appointmentId)) {
                    throw new IllegalStateException("A review for this appointment already exists");
                }
            } else {
                if (reviewRepository.existsByMaster_IdAndAuthorId(masterId, review.getAuthorId())) {
                    throw new IllegalStateException(
                            "Author has already left a review for this master");
                }
            }
        }

        validateRating(review.getRating());

        review.setMaster(master);
        review.setCreatedAt(OffsetDateTime.now());
        MasterReview saved = reviewRepository.save(review);
        log.info("Created review id={} for master id={} by author={} isManual={}",
                saved.getId(), masterId, saved.getAuthorId(), saved.getIsManual());
        return saved;
    }

    // ── Ручной отзыв от admin ─────────────────────────────────────────────────

    @Override
    @Transactional
    public MasterReview addManualReview(Long masterId, MasterReview review, Long adminTelegramId) {
        Assert.notNull(masterId,        "masterId must not be null");
        Assert.notNull(review,          "review must not be null");
        Assert.notNull(adminTelegramId, "adminTelegramId must not be null");
        Assert.notNull(review.getRating(), "rating must not be null");

        requireAdmin(adminTelegramId);

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Master not found id=" + masterId));

        validateRating(review.getRating());

        review.setMaster(master);
        review.setAuthorId(adminTelegramId.toString());
        review.setIsManual(true);
        review.setAppointment(null);
        review.setCreatedAt(OffsetDateTime.now());

        MasterReview saved = reviewRepository.save(review);
        log.info("Created MANUAL review id={} for master id={} by admin={}",
                saved.getId(), masterId, adminTelegramId);
        return saved;
    }

    // ── Чтение ────────────────────────────────────────────────────────────────

    @Override
    public MasterReview getById(Long reviewId) {
        Assert.notNull(reviewId, "reviewId must not be null");
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Review not found id=" + reviewId));
    }

    @Override
    public List<MasterReview> getByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId))
            throw new EntityNotFoundException("Master not found id=" + masterId);
        return reviewRepository.findByMasterIdOrderByCreatedAtDesc(masterId);
    }

    @Override
    public double getAverageRating(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId))
            throw new EntityNotFoundException("Master not found id=" + masterId);
        Double avg = reviewRepository.findAverageRatingByMasterId(masterId);
        return avg == null ? 0.0 : Math.round(avg * 100.0) / 100.0;
    }

    // ── Удаление ──────────────────────────────────────────────────────────────

    /**
     * Удалить без проверки прав.
     * Проверка роли ADMIN выполняется в MasterServiceImpl.deleteReview()
     * перед тем как он вызывает этот метод.
     * Двухаргументный overload delete(Long, Long) удалён — он нигде не вызывался.
     */
    @Override
    @Transactional
    public void delete(Long reviewId) {
        Assert.notNull(reviewId, "reviewId must not be null");
        if (!reviewRepository.existsById(reviewId))
            throw new EntityNotFoundException("Review not found id=" + reviewId);
        reviewRepository.deleteById(reviewId);
        log.info("Deleted review id={}", reviewId);
    }

    // ── Вспомогательные ───────────────────────────────────────────────────────

    private void validateRating(Integer rating) {
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5");
    }

    private void requireAdmin(Long telegramId) {
        var user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found: " + telegramId));
        if (!UserRole.ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("Only ADMIN can perform this action");
        }
    }
}