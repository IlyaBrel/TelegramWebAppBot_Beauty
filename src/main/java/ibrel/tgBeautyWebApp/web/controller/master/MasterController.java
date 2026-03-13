package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.MasterDto;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.service.master.MasterReviewService;
import ibrel.tgBeautyWebApp.service.master.MasterService;
import ibrel.tgBeautyWebApp.web.mapper.master.MasterMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final MasterService       masterService;
    private final MasterMapper        masterMapper;
    private final MasterReviewService reviewService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<MasterDto> create(@Valid @RequestBody MasterDto dto,
                                            @RequestParam Long adminTelegramId) {
        Master created = masterService.create(masterMapper.toEntity(dto), adminTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(
                masterMapper.toDto(created, reviewService.getAverageRating(created.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MasterDto> update(@PathVariable Long id,
                                            @Valid @RequestBody MasterDto dto,
                                            @RequestParam Long requesterTelegramId) {
        Master updated = masterService.update(id, masterMapper.toEntity(dto), requesterTelegramId);
        return ResponseEntity.ok(
                masterMapper.toDto(updated, reviewService.getAverageRating(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MasterDto> getById(@PathVariable Long id) {
        Master m = masterService.getById(id);
        return ResponseEntity.ok(
                masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }

    /**
     * Каталог мастеров.
     * Если передан adminTelegramId — возвращаем всех включая неактивных.
     * Иначе — только активных с фильтрами.
     */
    @GetMapping
    public ResponseEntity<List<MasterDto>> getAll(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Long adminTelegramId) {

        List<Master> masters;
        if (adminTelegramId != null) {
            masters = masterService.getAll();
        } else if (cityId != null && specialtyId != null) {
            masters = masterService.getByCityAndSpecialty(cityId, specialtyId);
        } else if (cityId != null) {
            masters = masterService.getByCity(cityId);
        } else if (specialtyId != null) {
            masters = masterService.getBySpecialty(specialtyId);
        } else {
            masters = masterService.getAllActive();
        }

        return ResponseEntity.ok(toDto(masters));
    }

    /**
     * Все профили мастера по telegramId пользователя.
     * GET /api/masters/by-user/{telegramId}
     */
    @GetMapping("/by-user/{telegramId}")
    public ResponseEntity<List<MasterDto>> getByUser(@PathVariable Long telegramId) {
        return ResponseEntity.ok(toDto(masterService.getAllByTelegramId(telegramId)));
    }

    /**
     * Создать дополнительный профиль мастера для существующего пользователя.
     * POST /api/masters/by-user/{telegramId}/profiles?profileName=Брови&adminTelegramId=...
     */
    @PostMapping("/by-user/{telegramId}/profiles")
    public ResponseEntity<MasterDto> createAdditionalProfile(
            @PathVariable Long telegramId,
            @RequestParam String profileName,
            @RequestParam Long adminTelegramId) {

        Master created = masterService.createAdditionalProfile(telegramId, profileName, adminTelegramId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/masters/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(
                masterMapper.toDto(created, reviewService.getAverageRating(created.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam Long adminTelegramId) {
        masterService.delete(id, adminTelegramId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<MasterDto> activate(@PathVariable Long id) {
        Master m = masterService.activate(id);
        return ResponseEntity.ok(masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<MasterDto> deactivate(@PathVariable Long id) {
        Master m = masterService.deactivate(id);
        return ResponseEntity.ok(masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }

    // ── Специализации и удобства ──────────────────────────────────────────────

    @PutMapping("/{id}/specialties")
    public ResponseEntity<MasterDto> setSpecialties(@PathVariable Long id,
                                                    @RequestBody List<Long> specialtyIds,
                                                    @RequestParam Long adminTelegramId) {
        Master m = masterService.addSpecialties(id, specialtyIds, adminTelegramId);
        return ResponseEntity.ok(masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }

    @PutMapping("/{id}/amenities")
    public ResponseEntity<MasterDto> setAmenities(@PathVariable Long id,
                                                  @RequestBody List<Long> amenityIds,
                                                  @RequestParam Long adminTelegramId) {
        Master m = masterService.addAmenities(id, amenityIds, adminTelegramId);
        return ResponseEntity.ok(masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }

    // ── Рейтинг ───────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/boosted-rating")
    public ResponseEntity<MasterDto> setBoostedRating(@PathVariable Long id,
                                                      @RequestParam Double rating,
                                                      @RequestParam Long adminTelegramId) {
        Master m = masterService.setBoostedRating(id, rating, adminTelegramId);
        return ResponseEntity.ok(masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<MasterDto> toDto(List<Master> masters) {
        return masters.stream()
                .map(m -> masterMapper.toDto(m, reviewService.getAverageRating(m.getId())))
                .collect(Collectors.toList());
    }
}