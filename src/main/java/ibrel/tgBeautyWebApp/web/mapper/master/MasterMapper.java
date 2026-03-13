package ibrel.tgBeautyWebApp.web.mapper.master;

import ibrel.tgBeautyWebApp.dto.master.MasterDto;
import ibrel.tgBeautyWebApp.model.master.Amenity;
import ibrel.tgBeautyWebApp.model.master.City;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.Specialty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MasterMapper {

    private final MasterAddressMapper      addressMapper;
    private final MasterPersonalDataMapper personalMapper;

    public MasterDto toDto(Master master, Double averageRating) {
        if (master == null) return null;

        List<String> specialtyNames = master.getSpecialties() == null ? List.of() :
                master.getSpecialties().stream()
                        .map(Specialty::getName)
                        .collect(Collectors.toList());

        List<String> amenityNames = master.getAmenities() == null ? List.of() :
                master.getAmenities().stream()
                        .map(Amenity::getName)
                        .collect(Collectors.toList());

        Double displayRating = (averageRating != null && averageRating > 0)
                ? averageRating
                : master.getBoostedRating();

        return MasterDto.builder()
                .id(master.getId())
                .telegramId(master.getTelegramId())
                .active(master.getActive())
                .imageUrl(master.getImageUrl())
                .profileName(master.getProfileName())
                .personalData(personalMapper.toDto(master.getPersonalData()))
                .address(addressMapper.toDto(master.getAddress()))
                .cityId(master.getCity() != null ? master.getCity().getId() : null)
                .cityName(master.getCity() != null ? master.getCity().getName() : null)
                .specialties(specialtyNames)
                .amenities(amenityNames)
                .averageRating(averageRating)
                .cachedAvgRating(master.getCachedAvgRating())
                .boostedRating(master.getBoostedRating())
                .displayRating(displayRating)
                .reviewCount(master.getReviewCount())
                .build();
    }

    public MasterDto toDto(Master master) {
        return toDto(master, null);
    }

    public Master toEntity(MasterDto dto, Double averageRating) {
        if (dto == null) return null;

        City cityStub = null;
        if (dto.getCityId() != null) {
            cityStub = new City();
            cityStub.setId(dto.getCityId());
        }

        return Master.builder()
                .id(dto.getId())
                .telegramId(dto.getTelegramId())
                .active(dto.getActive())
                .imageUrl(dto.getImageUrl())
                .profileName(dto.getProfileName())
                .personalData(personalMapper.toEntity(dto.getPersonalData()))
                .address(addressMapper.toEntity(dto.getAddress()))
                .city(cityStub)
                .build();
    }

    public Master toEntity(MasterDto dto) {
        return toEntity(dto, null);
    }
}