package ibrel.tgBeautyWebApp.web.controller.campaign;

import ibrel.tgBeautyWebApp.model.campaign.Campaign;
import ibrel.tgBeautyWebApp.model.campaign.Giveaway;
import ibrel.tgBeautyWebApp.service.campaign.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    // ── Акции (Campaign) ──────────────────────────────────────────────────────

    /** Все активные акции (публичный) */
    @GetMapping("/active")
    public ResponseEntity<List<Campaign>> getActive() {
        return ResponseEntity.ok(campaignService.getActive());
    }

    /** Все акции (для администратора) */
    @GetMapping
    public ResponseEntity<List<Campaign>> getAll() {
        return ResponseEntity.ok(campaignService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Campaign> create(@RequestBody Campaign campaign) {
        Campaign created = campaignService.create(campaign);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campaign> update(@PathVariable Long id, @RequestBody Campaign data) {
        return ResponseEntity.ok(campaignService.update(id, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Розыгрыши (Giveaway) ──────────────────────────────────────────────────

    /** Все активные розыгрыши (публичный) */
    @GetMapping("/giveaways/active")
    public ResponseEntity<List<Giveaway>> getActiveGiveaways() {
        return ResponseEntity.ok(campaignService.getActiveGiveaways());
    }

    /** Все розыгрыши (для администратора) */
    @GetMapping("/giveaways")
    public ResponseEntity<List<Giveaway>> getAllGiveaways() {
        return ResponseEntity.ok(campaignService.getAllGiveaways());
    }

    @GetMapping("/giveaways/{id}")
    public ResponseEntity<Giveaway> getGiveawayById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getGiveawayById(id));
    }

    @PostMapping("/giveaways")
    public ResponseEntity<Giveaway> createGiveaway(@RequestBody Giveaway giveaway) {
        Giveaway created = campaignService.createGiveaway(giveaway);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/giveaways/{id}")
    public ResponseEntity<Giveaway> updateGiveaway(@PathVariable Long id, @RequestBody Giveaway data) {
        return ResponseEntity.ok(campaignService.updateGiveaway(id, data));
    }

    @DeleteMapping("/giveaways/{id}")
    public ResponseEntity<Void> deleteGiveaway(@PathVariable Long id) {
        campaignService.deleteGiveaway(id);
        return ResponseEntity.noContent().build();
    }

    /** Зарегистрировать участие пользователя в розыгрыше */
    @PostMapping("/giveaways/{id}/join")
    public ResponseEntity<Giveaway> joinGiveaway(@PathVariable Long id,
                                                  @RequestParam Long telegramId) {
        return ResponseEntity.ok(campaignService.joinGiveaway(id, telegramId));
    }
}
