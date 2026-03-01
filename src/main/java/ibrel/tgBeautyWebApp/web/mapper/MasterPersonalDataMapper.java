package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.MasterPersonalDataDto;
import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import org.springframework.stereotype.Component;

@Component
public class MasterPersonalDataMapper {

    public MasterPersonalDataDto toDto(MasterPersonalData pd) {
        if (pd == null) return null;
        return MasterPersonalDataDto.builder()
                .id(pd.getId())
                .firstName(pd.getFirstName())
                .lastName(pd.getLastName())
                .description(pd.getDescription())
                .phone(pd.getPhone())
                .experienceYears(pd.getExperienceYears())
                .completedJobs(pd.getCompletedJobs())
                .instUserId(pd.getInstUserId())
                .build();
    }

    public MasterPersonalData toEntity(MasterPersonalDataDto dto) {
        if (dto == null) return null;
        MasterPersonalData pd = new MasterPersonalData();
        pd.setId(dto.getId());
        pd.setFirstName(dto.getFirstName());
        pd.setLastName(dto.getLastName());
        pd.setDescription(dto.getDescription());
        pd.setPhone(dto.getPhone());
        pd.setExperienceYears(dto.getExperienceYears());
        pd.setCompletedJobs(dto.getCompletedJobs());
        pd.setInstUserId(dto.getInstUserId());
        return pd;
    }
}
