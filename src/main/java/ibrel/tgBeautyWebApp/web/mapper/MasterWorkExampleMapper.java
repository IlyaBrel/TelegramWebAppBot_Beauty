package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.MasterWorkExampleDto;
import ibrel.tgBeautyWebApp.model.master.MasterWorkExample;
import org.springframework.stereotype.Component;

@Component
public class MasterWorkExampleMapper {

    public MasterWorkExampleDto toDto(MasterWorkExample e) {
        if (e == null) return null;
        return MasterWorkExampleDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .imageUrl(e.getImageUrl())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public MasterWorkExample toEntity(MasterWorkExampleDto dto) {
        if (dto == null) return null;
        MasterWorkExample e = new MasterWorkExample();
        e.setId(dto.getId());
        e.setTitle(dto.getTitle());
        e.setDescription(dto.getDescription());
        e.setImageUrl(dto.getImageUrl());
        // createdAt устанавливается в сервисе при создании
        return e;
    }
}
