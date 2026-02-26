package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.MasterReviewRepository;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.service.master.MasterReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterReviewServiceImpl implements MasterReviewService {

    private final MasterRepository masterRepository;
    private final MasterReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository; // для возможной валидации прав (опционально)

    @Override
    @Transactional
    public MasterReview addReview(Long masterId, MasterReview review) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(review, "review must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Опционально: проверка, что автор оставил хотя бы одну запись у мастера (если бизнес-требование)
        // boolean hasAppointment = appointmentRepository.findByMasterId(masterId).stream()
        //        .anyMatch(a -> a.getUser() != null && a.getUser().getName().equals(review.getAuthorName()));
        // if (!hasAppointment) throw new IllegalStateException("User must have appointment to leave review");

        review.setMaster(master);
        MasterReview saved = reviewRepository.save(review);
        log.info("Added review id={} for master id={} rating={}", saved.getId(), masterId, saved.getRating());
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
        masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));
        return reviewRepository.findByMasterId(masterId);
    }

    @Override
    @Transactional
    public void delete(Long reviewId) {
        Assert.notNull(reviewId, "reviewId must not be null");
        if (!reviewRepository.existsById(reviewId)) {
            throw new EntityNotFoundException("Review not found id=" + reviewId);
        }
        reviewRepository.deleteById(reviewId);
        log.info("Deleted review id={}", reviewId);
    }

    @Override
    public double getAverageRating(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        List<MasterReview> reviews = reviewRepository.findByMasterId(masterId);
        if (reviews.isEmpty()) return 0.0;

        DoubleSummaryStatistics stats = reviews.stream()
                .filter(r -> r.getRating() != null)
                .collect(Collectors.summarizingDouble(MasterReview::getRating));

        double avg = stats.getAverage();
        log.debug("Calculated average rating={} for masterId={}", avg, masterId);
        return avg;
    }
}
