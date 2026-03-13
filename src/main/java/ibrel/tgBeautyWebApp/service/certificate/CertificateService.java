package ibrel.tgBeautyWebApp.service.certificate;

import ibrel.tgBeautyWebApp.model.certificate.Certificate;

import java.util.List;

public interface CertificateService {

    /**
     * Мастер выдаёт сертификат пользователю.
     * Привязка — к конкретному профилю мастера (masterId), не к telegramId.
     */
    Certificate issue(Long masterId, Long recipientTelegramId,
                      List<Long> serviceIds, Integer validDays);

    Certificate findByCode(String code);

    List<Certificate> getByRecipient(Long recipientTelegramId);

    List<Certificate> getActiveByRecipient(Long recipientTelegramId);

    /** Сертификаты конкретного профиля мастера */
    List<Certificate> getByMaster(Long masterId);

    /** Отменить сертификат. requesterTelegramId — для проверки доступа. */
    Certificate cancel(String code, Long requesterTelegramId);
}