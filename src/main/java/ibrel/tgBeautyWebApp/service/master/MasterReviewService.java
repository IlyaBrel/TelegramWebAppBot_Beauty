package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.master.MasterReview;

import java.util.List;

public interface MasterReviewService {

    /**
     * Добавить реальный отзыв от клиента.
     * Проверяет дубликаты по authorId и по appointmentId.
     */
    MasterReview addReview(Long masterId, MasterReview review);

    /**
     * Добавить ручной отзыв (isManual = true).
     * Не проверяет дубликаты по authorId — admin может добавить несколько.
     * Проверяет роль по adminTelegramId.
     */
    MasterReview addManualReview(Long masterId, MasterReview review, Long adminTelegramId);

    MasterReview getById(Long reviewId);

    List<MasterReview> getByMaster(Long masterId);

    /**
     * Удалить отзыв без дополнительной проверки прав.
     * Проверка роли ADMIN выполняется в MasterServiceImpl.deleteReview()
     * перед тем как он вызывает этот метод.
     */
    void delete(Long reviewId);

    double getAverageRating(Long masterId);
}