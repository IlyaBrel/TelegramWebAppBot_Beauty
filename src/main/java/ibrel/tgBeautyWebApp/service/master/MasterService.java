package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterReview;

import java.util.List;

public interface MasterService {

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** Создать мастера вручную (admin). */
    Master create(Master master, Long adminTelegramId);

    /**
     * Автоматически создать первый профиль мастера на основе TG-профиля пользователя.
     * Вызывается при смене роли пользователя на MASTER.
     * Если профиль уже существует — возвращает первый существующий (идемпотентно).
     */
    Master createFromUser(UserTG user, Long adminTelegramId);

    /**
     * Создать дополнительный профиль мастера для пользователя с уже существующими профилями.
     * profileName обязателен и должен быть уникальным для данного telegramId.
     */
    Master createAdditionalProfile(Long telegramId, String profileName, Long adminTelegramId);

    Master update(Long id, Master master, Long requesterTelegramId);

    Master getById(Long id);

    /** Первый профиль мастера по telegramId (для обратной совместимости). */
    Master getByTelegramId(Long telegramId);

    /** Все профили мастера по telegramId. */
    List<Master> getAllByTelegramId(Long telegramId);

    /** Все мастера включая неактивных — только для admin. */
    List<Master> getAll();

    List<Master> getAllActive();
    List<Master> getByCity(Long cityId);
    List<Master> getBySpecialty(Long specialtyId);
    List<Master> getByCityAndSpecialty(Long cityId, Long specialtyId);

    void delete(Long id, Long adminTelegramId);

    Master activate(Long id);
    Master deactivate(Long id);

    // ── Специализации и удобства ──────────────────────────────────────────────

    Master addSpecialties(Long masterId, List<Long> specialtyIds, Long adminTelegramId);
    Master addAmenities(Long masterId, List<Long> amenityIds, Long adminTelegramId);

    // ── Рейтинг ───────────────────────────────────────────────────────────────

    Master setBoostedRating(Long masterId, Double rating, Long adminTelegramId);

    // ── Отзывы ────────────────────────────────────────────────────────────────

    MasterReview addManualReview(Long masterId, MasterReview review, Long adminTelegramId);
    void deleteReview(Long reviewId, Long adminTelegramId);
}