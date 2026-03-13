package ibrel.tgBeautyWebApp.web.mapper.catalog;

import ibrel.tgBeautyWebApp.dto.catalog.*;
import ibrel.tgBeautyWebApp.model.Promotion;
import ibrel.tgBeautyWebApp.model.master.Amenity;
import ibrel.tgBeautyWebApp.model.master.City;
import ibrel.tgBeautyWebApp.model.master.Specialty;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {

    // ── City ──────────────────────────────────────────────────────────────────

    public CityDto toDto(City city) {
        if (city == null) return null;
        return CityDto.builder()
                .id(city.getId())
                .name(city.getName())
                .region(city.getRegion())
                .active(city.getActive())
                .build();
    }

    public City toEntity(CityDto dto) {
        if (dto == null) return null;
        City city = new City();
        city.setId(dto.getId());
        city.setName(dto.getName());
        city.setRegion(dto.getRegion());
        city.setActive(dto.getActive());
        return city;
    }

    // ── Specialty ─────────────────────────────────────────────────────────────

    public SpecialtyDto toDto(Specialty specialty) {
        if (specialty == null) return null;
        return SpecialtyDto.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .icon(specialty.getIcon())
                .active(specialty.getActive())
                .build();
    }

    public Specialty toEntity(SpecialtyDto dto) {
        if (dto == null) return null;
        Specialty specialty = new Specialty();
        specialty.setId(dto.getId());
        specialty.setName(dto.getName());
        specialty.setDescription(dto.getDescription());
        specialty.setIcon(dto.getIcon());
        specialty.setActive(dto.getActive());
        return specialty;
    }

    // ── Amenity ───────────────────────────────────────────────────────────────

    public AmenityDto toDto(Amenity amenity) {
        if (amenity == null) return null;
        return AmenityDto.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .icon(amenity.getIcon())
                .active(amenity.getActive())
                .build();
    }

    public Amenity toEntity(AmenityDto dto) {
        if (dto == null) return null;
        Amenity amenity = new Amenity();
        amenity.setId(dto.getId());
        amenity.setName(dto.getName());
        amenity.setDescription(dto.getDescription());
        amenity.setIcon(dto.getIcon());
        amenity.setActive(dto.getActive());
        return amenity;
    }

    // ── Promotion ─────────────────────────────────────────────────────────────

    public PromotionDto toDto(Promotion promotion) {
        if (promotion == null) return null;
        return PromotionDto.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .imageUrl(promotion.getImageUrl())
                .actionUrl(promotion.getActionUrl())
                .type(promotion.getType())           // Promotion.PromotionType enum
                .sortOrder(promotion.getSortOrder())
                .active(promotion.getActive())
                .expiresAt(promotion.getExpiresAt())
                .createdAt(promotion.getCreatedAt())
                .build();
    }

    public Promotion toEntity(PromotionDto dto) {
        if (dto == null) return null;
        Promotion promotion = new Promotion();
        promotion.setId(dto.getId());
        promotion.setTitle(dto.getTitle());
        promotion.setDescription(dto.getDescription());
        promotion.setImageUrl(dto.getImageUrl());
        promotion.setActionUrl(dto.getActionUrl());
        promotion.setType(dto.getType());            // Promotion.PromotionType enum
        promotion.setSortOrder(dto.getSortOrder());
        promotion.setActive(dto.getActive());
        promotion.setExpiresAt(dto.getExpiresAt());
        // createdAt устанавливается в CatalogService.createPromotion()
        return promotion;
    }
}