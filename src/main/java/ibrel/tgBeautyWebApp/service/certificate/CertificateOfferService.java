package ibrel.tgBeautyWebApp.service.certificate;

import ibrel.tgBeautyWebApp.model.certificate.CertificateOffer;

import java.util.List;

public interface CertificateOfferService {

    /**
     * Создать оффер для конкретного профиля мастера.
     * masterId — ID профиля (не telegramId), чтобы не путать профили.
     */
    CertificateOffer create(Long masterId, CertificateOffer offer, Long createdByTelegramId);

    /** Admin создаёт оффер для конкретного профиля мастера */
    CertificateOffer createForMaster(Long masterId, CertificateOffer offer, Long adminTelegramId);

    /** Обновить оффер. requesterTelegramId — для проверки доступа. */
    CertificateOffer update(Long offerId, CertificateOffer updated, Long requesterTelegramId);

    /** Скрыть / показать оффер */
    CertificateOffer setActive(Long offerId, boolean active, Long requesterTelegramId);

    /** Удалить оффер (только если нет PENDING заявок) */
    void delete(Long offerId, Long requesterTelegramId);

    CertificateOffer getById(Long offerId);

    /** Все офферы профиля мастера (включая неактивные) */
    List<CertificateOffer> getByMaster(Long masterId);

    /** Активные офферы профиля мастера (для клиентов) */
    List<CertificateOffer> getAvailableByMaster(Long masterId);

    /** Активные офферы по городу */
    List<CertificateOffer> getAvailableByCity(Long cityId);
}