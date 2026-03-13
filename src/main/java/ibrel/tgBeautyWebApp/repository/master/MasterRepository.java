package ibrel.tgBeautyWebApp.repository.master;

import ibrel.tgBeautyWebApp.model.master.Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master, Long> {

    // ── Поиск по telegramId — теперь возвращает список (несколько профилей) ──

    /** Все профили мастера по telegramId */
    List<Master> findAllByTelegramId(Long telegramId);

    /** Первый профиль — для обратной совместимости (например, getCertificate) */
    Optional<Master> findFirstByTelegramId(Long telegramId);

    boolean existsByTelegramId(Long telegramId);

    /** Проверка: есть ли у пользователя профиль с данным именем */
    boolean existsByTelegramIdAndProfileName(Long telegramId, String profileName);

    boolean existsById(Long id);

    List<Master> findAllByOrderByCachedAvgRatingDesc();

    List<Master> findByActiveTrueOrderByCachedAvgRatingDesc();

    List<Master> findByActiveTrueAndCity_IdOrderByCachedAvgRatingDesc(Long cityId);

    @Query("select m from Master m join m.specialties s " +
            "where m.active = true and s.id = :specialtyId " +
            "order by m.cachedAvgRating desc")
    List<Master> findActiveBySpecialtyId(@Param("specialtyId") Long specialtyId);

    @Query("select m from Master m join m.specialties s " +
            "where m.active = true " +
            "and m.city.id = :cityId " +
            "and s.id = :specialtyId " +
            "order by m.cachedAvgRating desc")
    List<Master> findActiveByCity_IdAndSpecialtyId(
            @Param("cityId")      Long cityId,
            @Param("specialtyId") Long specialtyId);

    @Query("select m from Master m " +
            "join FavoriteMaster f on f.master.id = m.id " +
            "where f.userTelegramId = :userTelegramId " +
            "and m.active = true " +
            "order by m.cachedAvgRating desc")
    List<Master> findFavoritesByUserTelegramId(@Param("userTelegramId") Long userTelegramId);
}