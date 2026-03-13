package ibrel.tgBeautyWebApp.web.controller.catalog;

import ibrel.tgBeautyWebApp.dto.catalog.*;
import ibrel.tgBeautyWebApp.model.master.Amenity;
import ibrel.tgBeautyWebApp.model.master.City;
import ibrel.tgBeautyWebApp.model.master.Specialty;
import ibrel.tgBeautyWebApp.model.Promotion;
import ibrel.tgBeautyWebApp.service.catalog.CatalogService;
import ibrel.tgBeautyWebApp.web.mapper.catalog.CatalogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final CatalogMapper  mapper;

    // ── Города ───────────────────────────────────────────────────────────────

    /** Список активных городов (публичный) */
    @GetMapping("/cities")
    public ResponseEntity<List<CityDto>> getCities() {
        return ResponseEntity.ok(
                catalogService.getCities().stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    @PostMapping("/cities")
    public ResponseEntity<CityDto> createCity(@RequestBody CityDto dto,
                                              @RequestParam Long adminTelegramId) {
        City created = catalogService.createCity(mapper.toEntity(dto), adminTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PutMapping("/cities/{id}")
    public ResponseEntity<CityDto> updateCity(@PathVariable Long id,
                                              @RequestBody CityDto dto,
                                              @RequestParam Long adminTelegramId) {
        City updated = catalogService.updateCity(id, mapper.toEntity(dto), adminTelegramId);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id,
                                           @RequestParam Long adminTelegramId) {
        catalogService.deleteCity(id, adminTelegramId);
        return ResponseEntity.noContent().build();
    }

    // ── Специализации ─────────────────────────────────────────────────────────

    /** Список активных специализаций (публичный) */
    @GetMapping("/specialties")
    public ResponseEntity<List<SpecialtyDto>> getSpecialties() {
        return ResponseEntity.ok(
                catalogService.getSpecialties().stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    @PostMapping("/specialties")
    public ResponseEntity<SpecialtyDto> createSpecialty(@RequestBody SpecialtyDto dto,
                                                        @RequestParam Long adminTelegramId) {
        Specialty created = catalogService.createSpecialty(mapper.toEntity(dto), adminTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PutMapping("/specialties/{id}")
    public ResponseEntity<SpecialtyDto> updateSpecialty(@PathVariable Long id,
                                                        @RequestBody SpecialtyDto dto,
                                                        @RequestParam Long adminTelegramId) {
        Specialty updated = catalogService.updateSpecialty(id, mapper.toEntity(dto), adminTelegramId);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/specialties/{id}")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable Long id,
                                                @RequestParam Long adminTelegramId) {
        catalogService.deleteSpecialty(id, adminTelegramId);
        return ResponseEntity.noContent().build();
    }

    // ── Удобства ──────────────────────────────────────────────────────────────

    /** Список активных удобств (публичный) */
    @GetMapping("/amenities")
    public ResponseEntity<List<AmenityDto>> getAmenities() {
        return ResponseEntity.ok(
                catalogService.getAmenities().stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    @PostMapping("/amenities")
    public ResponseEntity<AmenityDto> createAmenity(@RequestBody AmenityDto dto,
                                                    @RequestParam Long adminTelegramId) {
        Amenity created = catalogService.createAmenity(mapper.toEntity(dto), adminTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PutMapping("/amenities/{id}")
    public ResponseEntity<AmenityDto> updateAmenity(@PathVariable Long id,
                                                    @RequestBody AmenityDto dto,
                                                    @RequestParam Long adminTelegramId) {
        Amenity updated = catalogService.updateAmenity(id, mapper.toEntity(dto), adminTelegramId);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/amenities/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id,
                                              @RequestParam Long adminTelegramId) {
        catalogService.deleteAmenity(id, adminTelegramId);
        return ResponseEntity.noContent().build();
    }

    // ── Акции ─────────────────────────────────────────────────────────────────

    /** Активные акции (публичный, не требует adminTelegramId) */
    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionDto>> getActivePromotions() {
        return ResponseEntity.ok(
                catalogService.getActivePromotions().stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Все акции включая неактивные — только для admin.
     * adminTelegramId обязателен — сервис проверяет роль.
     */
    @GetMapping("/promotions/all")
    public ResponseEntity<List<PromotionDto>> getAllPromotions(@RequestParam Long adminTelegramId) {
        return ResponseEntity.ok(
                catalogService.getAllPromotions(adminTelegramId).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    @PostMapping("/promotions")
    public ResponseEntity<PromotionDto> createPromotion(@RequestBody PromotionDto dto,
                                                        @RequestParam Long adminTelegramId) {
        Promotion created = catalogService.createPromotion(mapper.toEntity(dto), adminTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<PromotionDto> updatePromotion(@PathVariable Long id,
                                                        @RequestBody PromotionDto dto,
                                                        @RequestParam Long adminTelegramId) {
        Promotion updated = catalogService.updatePromotion(id, mapper.toEntity(dto), adminTelegramId);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id,
                                                @RequestParam Long adminTelegramId) {
        catalogService.deletePromotion(id, adminTelegramId);
        return ResponseEntity.noContent().build();
    }
}