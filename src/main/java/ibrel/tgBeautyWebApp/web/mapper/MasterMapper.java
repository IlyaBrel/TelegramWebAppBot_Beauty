package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.MasterDto;
import ibrel.tgBeautyWebApp.model.master.Master;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MasterMapper {

    private final MasterAddressMapper addressMapper;
    private final MasterPersonalDataMapper personalMapper;

    public MasterDto toDto(Master master, Double averageRating) {
        if (master == null) return null;
        return MasterDto.builder()
                .id(master.getId())
                .active(master.getActive())
                .imageUrl(master.getImageUrl())
                .personalData(personalMapper.toDto(master.getPersonalData()))
                .address(addressMapper.toDto(master.getAddress()))
                .experienceYears(master.getPersonalData() != null ? master.getPersonalData().getExperienceYears() : null)
                .completedJobs(master.getPersonalData() != null ? master.getPersonalData().getCompletedJobs() : null)
                .averageRating(averageRating)
                .build();
    }

    public MasterDto toDto(Master master) {
        return toDto(master, null);
    }

    public Master toEntity(MasterDto dto) {
        if (dto == null) return null;
        Master m = new Master();
        m.setId(dto.getId());
        m.setActive(dto.getActive());
        m.setImageUrl(dto.getImageUrl());
        // personalData/address handled by specialized services to keep SRP
        return m;
    }
}
