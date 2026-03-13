package ibrel.tgBeautyWebApp.service.campaign.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.campaign.Campaign;
import ibrel.tgBeautyWebApp.model.campaign.Giveaway;
import ibrel.tgBeautyWebApp.repository.campaign.CampaignRepository;
import ibrel.tgBeautyWebApp.repository.campaign.GiveawayRepository;
import ibrel.tgBeautyWebApp.repository.user.UserRepository;
import ibrel.tgBeautyWebApp.service.campaign.CampaignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepo;
    private final GiveawayRepository giveawayRepo;
    private final UserRepository userRepo;

    // ── Campaign ──────────────────────────────────────────────────────────────

    @Override
    public Campaign getById(Long id) {
        return campaignRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found id=" + id));
    }

    @Override
    public List<Campaign> getAll() {
        return campaignRepo.findAll();
    }

    @Override
    public List<Campaign> getActive() {
        return campaignRepo.findActive(LocalDate.now());
    }

    @Override
    @Transactional
    public Campaign create(Campaign campaign) {
        Campaign saved = campaignRepo.save(campaign);
        log.info("Campaign created id={} type={}", saved.getId(), saved.getType());
        return saved;
    }

    @Override
    @Transactional
    public Campaign update(Long id, Campaign data) {
        Campaign existing = getById(id);
        existing.setType(data.getType());
        existing.setTitle(data.getTitle());
        existing.setBannerUrl(data.getBannerUrl());
        existing.setStartDate(data.getStartDate());
        existing.setEndDate(data.getEndDate());
        existing.setActive(data.getActive());
        existing.setMaster(data.getMaster());
        return campaignRepo.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        campaignRepo.deleteById(id);
        log.info("Campaign deleted id={}", id);
    }

    // ── Giveaway ──────────────────────────────────────────────────────────────

    @Override
    public Giveaway getGiveawayById(Long id) {
        return giveawayRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Giveaway not found id=" + id));
    }

    @Override
    public List<Giveaway> getAllGiveaways() {
        return giveawayRepo.findAll();
    }

    @Override
    public List<Giveaway> getActiveGiveaways() {
        return giveawayRepo.findActive(LocalDate.now());
    }

    @Override
    @Transactional
    public Giveaway createGiveaway(Giveaway giveaway) {
        Giveaway saved = giveawayRepo.save(giveaway);
        log.info("Giveaway created id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Giveaway updateGiveaway(Long id, Giveaway data) {
        Giveaway existing = getGiveawayById(id);
        existing.setBannerUrl(data.getBannerUrl());
        existing.setConditions(data.getConditions());
        existing.setStartDate(data.getStartDate());
        existing.setEndDate(data.getEndDate());
        existing.setRequiresConfirmation(data.getRequiresConfirmation());
        existing.setActive(data.getActive());
        return giveawayRepo.save(existing);
    }

    @Override
    @Transactional
    public void deleteGiveaway(Long id) {
        giveawayRepo.deleteById(id);
        log.info("Giveaway deleted id={}", id);
    }

    @Override
    @Transactional
    public Giveaway joinGiveaway(Long giveawayId, Long telegramId) {
        Giveaway giveaway = getGiveawayById(giveawayId);
        UserTG user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + telegramId));

        boolean alreadyJoined = giveaway.getParticipants() != null &&
                giveaway.getParticipants().stream()
                        .anyMatch(p -> p.getTelegramId().equals(telegramId));

        if (alreadyJoined) {
            throw new IllegalStateException("User already joined giveaway id=" + giveawayId);
        }

        if (giveaway.getParticipants() == null) {
            giveaway.setParticipants(new java.util.ArrayList<>());
        }
        giveaway.getParticipants().add(user);
        Giveaway saved = giveawayRepo.save(giveaway);
        log.info("User telegramId={} joined giveaway id={}", telegramId, giveawayId);
        return saved;
    }
}
