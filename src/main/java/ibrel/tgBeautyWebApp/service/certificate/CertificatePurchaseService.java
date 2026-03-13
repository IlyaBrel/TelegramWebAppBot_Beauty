package ibrel.tgBeautyWebApp.service.certificate;

import ibrel.tgBeautyWebApp.model.certificate.Certificate;
import ibrel.tgBeautyWebApp.model.certificate.CertificatePurchaseRequest;

import java.util.List;

public interface CertificatePurchaseService {

    /**
     * Пользователь создаёт заявку на покупку оффера.
     */
    CertificatePurchaseRequest requestPurchase(Long buyerTelegramId, Long offerId, String comment);

    /**
     * Мастер одобряет оплату → выпускается Certificate.
     * masterTelegramId — для проверки, что одобряет именно владелец профиля.
     */
    Certificate approvePurchase(Long requestId, Long masterTelegramId);

    /**
     * Мастер отклоняет заявку.
     * masterTelegramId — для проверки доступа.
     */
    CertificatePurchaseRequest rejectPurchase(Long requestId, Long masterTelegramId, String reason);

    /**
     * Пользователь отменяет свою заявку (только PENDING).
     */
    CertificatePurchaseRequest cancelPurchase(Long requestId, Long buyerTelegramId);

    List<CertificatePurchaseRequest> getByBuyer(Long buyerTelegramId);

    /**
     * Входящие PENDING заявки для конкретного профиля мастера.
     * masterId — ID профиля, не telegramId.
     */
    List<CertificatePurchaseRequest> getPendingForMaster(Long masterId);

    /**
     * Все входящие заявки (история) конкретного профиля мастера.
     * masterId — ID профиля, не telegramId.
     */
    List<CertificatePurchaseRequest> getAllForMaster(Long masterId);
}