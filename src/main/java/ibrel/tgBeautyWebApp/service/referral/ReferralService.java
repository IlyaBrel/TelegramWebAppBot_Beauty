package ibrel.tgBeautyWebApp.service.referral;

public interface ReferralService {

    /** Генерация уникального 6-символьного кода */
    String generateInviteCode();

    /**
     * Зарегистрировать факт использования реферального кода при регистрации.
     * Увеличивает invitedUsersCount у пригласившего.
     */
    void handleReferral(Long newUserTelegramId, String inviteCode);

    /**
     * Вызывается при завершении заказа (COMPLETED).
     * Увеличивает invitedUsersOrdersCount у пригласившего и проверяет задания.
     */
    void handleOrderCompleted(Long clientTelegramId);
}
