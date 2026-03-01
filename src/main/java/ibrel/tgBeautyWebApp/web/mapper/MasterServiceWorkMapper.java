// src/main/java/ibrel/tgBeautyWebApp/web/mapper/MasterServiceWorkMapper.java
package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.*;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.enums.MasterServiceType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MasterServiceWorkMapper {

    public MasterServiceWorkDto toDto(MasterServiceWork s) {
        if (s == null) return null;
        return MasterServiceWorkDto.builder()
                .id(s.getId())
                .masterId(s.getMaster() != null ? s.getMaster().getId() : null)
                .name(s.getName())
                .type(s.getType() != null ? s.getType().name() : null)
                .description(s.getDescription())
                .fixedDetails(toFixedDto(s.getFixedDetails()))
                .variableDetails(toVariableDtoList(s.getVariableDetails()))
                .build();
    }

    public MasterServiceWork toEntity(MasterServiceWorkDto dto) {
        if (dto == null) return null;
        MasterServiceWork s = new MasterServiceWork();
        s.setId(dto.getId());
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());
        if (dto.getType() != null) {
            try {
                s.setType(MasterServiceType.valueOf(dto.getType()));
            } catch (IllegalArgumentException ignored) { /* validation in service */ }
        }
        s.setFixedDetails(toFixedEntity(dto.getFixedDetails()));
        s.setVariableDetails(toVariableEntityList(dto.getVariableDetails()));
        return s;
    }

    private FixedServiceDetailsDto toFixedDto(FixedServiceDetails f) {
        if (f == null) return null;
        return FixedServiceDetailsDto.builder()
                .id(f.getId())
                .durationMinutes(f.getDurationMinutes())
                .price(f.getPrice())
                .description(f.getDescription())
                .build();
    }

    private FixedServiceDetails toFixedEntity(FixedServiceDetailsDto dto) {
        if (dto == null) return null;
        FixedServiceDetails f = new FixedServiceDetails();
        f.setId(dto.getId());
        f.setDurationMinutes(dto.getDurationMinutes());
        f.setPrice(dto.getPrice());
        f.setDescription(dto.getDescription());
        return f;
    }

    private VariableServiceDetailsDto toVariableDto(VariableServiceDetails v) {
        if (v == null) return null;
        return VariableServiceDetailsDto.builder()
                .id(v.getId())
                .factorName(v.getFactorName())
                .factorValue(v.getFactorValue())
                .durationMinutes(v.getDurationMinutes())
                .price(v.getPrice())
                .description(v.getDescription())
                .build();
    }

    private VariableServiceDetails toVariableEntity(VariableServiceDetailsDto dto) {
        if (dto == null) return null;
        VariableServiceDetails v = new VariableServiceDetails();
        v.setId(dto.getId());
        v.setFactorName(dto.getFactorName());
        v.setFactorValue(dto.getFactorValue());
        v.setDurationMinutes(dto.getDurationMinutes());
        v.setPrice(dto.getPrice());
        v.setDescription(dto.getDescription());
        return v;
    }

    private List<VariableServiceDetailsDto> toVariableDtoList(List<VariableServiceDetails> list) {
        if (list == null) return null;
        return list.stream().map(this::toVariableDto).collect(Collectors.toList());
    }

    private List<VariableServiceDetails> toVariableEntityList(List<VariableServiceDetailsDto> list) {
        if (list == null) return null;
        return list.stream().map(this::toVariableEntity).collect(Collectors.toList());
    }
}
