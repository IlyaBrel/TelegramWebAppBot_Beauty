package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.MasterReviewRepository;
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

    private final MasterRepository masterRepository;
    private final MasterReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public MasterReview addReview(Long masterId, MasterReview review) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(review, "review must not be null");
        Assert.notNull(review.getAuthorId(), "authorId must not be null");
        Assert.notNull(review.getRating(), "rating must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        // Если отзыв привязан к appointment — проверяем, что такая запись существует и принадлежит мастеру
        if (review.getAppointment() != null && review.getAppointment().getId() != null) {
            Long appointmentId = review.getAppointment().getId();
            boolean appointmentExists = appointmentRepository.existsById(appointmentId);
            if (!appointmentExists) {
                throw new EntityNotFoundException("Appointment not found id=" + appointmentId);
            }
            // Дополнительно можно проверить принадлежность appointment.master == masterId, если модель Appointment хранит мастера
            // Пример:
            appointmentRepository.findById(appointmentId).ifPresent(a -> {
                if (a.getMaster() == null || !Objects.equals(a.getMaster().getId(), masterId)) {
                    throw new IllegalArgumentException("Appointment does not belong to the specified master");
                }
            });

            // Защита от повторного отзыва по одной записи
            if (reviewRepository.existsByAppointment_Id(appointmentId)) {
                throw new IllegalStateException("A review for this appointment already exists");
            }
        } else {
            // Если нет appointmentId, можно ограничить частоту отзывов от одного автора к одному мастеру
            if (reviewRepository.existsByMaster_IdAndAuthorId(masterId, review.getAuthorId())) {
                throw new IllegalStateException("Author has already left a review for this master");
            }
        }

        // Валидация рейтинга
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }

        review.setMaster(master);
        review.setCreatedAt(OffsetDateTime.now());
        MasterReview saved = reviewRepository.save(review);
        log.info("Created review id={} for master id={} by author={}", saved.getId(), masterId, saved.getAuthorId());
        return saved;
    }

    @Override
    public MasterReview getById(Long reviewId) {
        Assert.notNull(reviewId, "reviewId must not be null");
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found id=" + reviewId));
    }

    @Override
    public List<MasterReview> getByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId)) throw new EntityNotFoundException("Master not found id=" + masterId);
        return reviewRepository.findByMasterIdOrderByCreatedAtDesc(masterId);
    }

    @Override
    @Transactional
    public void delete(Long reviewId) {
        Assert.notNull(reviewId, "reviewId must not be null");
        if (!reviewRepository.existsById(reviewId)) throw new EntityNotFoundException("Review not found id=" + reviewId);
        reviewRepository.deleteById(reviewId);
        log.info("Deleted review id={}", reviewId);
    }

    @Override
    public double getAverageRating(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId)) throw new EntityNotFoundException("Master not found id=" + masterId);
        Double avg = reviewRepository.findAverageRatingByMasterId(masterId);
        return avg == null ? 0.0 : Math.round(avg * 100.0) / 100.0; // округляем до 2 знаков
    }
}
