package ibrel.tgBeautyWebApp.service.campaign;

import ibrel.tgBeautyWebApp.model.campaign.Campaign;
import ibrel.tgBeautyWebApp.model.campaign.Giveaway;

import java.util.List;

public interface CampaignService {

    Campaign getById(Long id);

    List<Campaign> getAll();

    List<Campaign> getActive();

    Campaign create(Campaign campaign);

    Campaign update(Long id, Campaign data);

    void delete(Long id);

    // ── Giveaway ─────────────────────────────────────────────────────────────

    Giveaway getGiveawayById(Long id);

    List<Giveaway> getAllGiveaways();

    List<Giveaway> getActiveGiveaways();

    Giveaway createGiveaway(Giveaway giveaway);

    Giveaway updateGiveaway(Long id, Giveaway data);

    void deleteGiveaway(Long id);

    /** Зарегистрировать участие пользователя в розыгрыше */
    Giveaway joinGiveaway(Long giveawayId, Long telegramId);
}
