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
                .telegramId(master.getTelegramId())
                .active(master.getActive())
                .imageUrl(master.getImageUrl())
                .personalData(personalMapper.toDto(master.getPersonalData()))
                .address(addressMapper.toDto(master.getAddress()))
                .averageRating(averageRating)
                .build();
    }

    public MasterDto toDto(Master master) {
        return toDto(master, null);
    }



    public Master toEntity(MasterDto master, Double averageRating) {
        if (master == null) return null;
        return Master.builder()
                .id(master.getId())
                .telegramId(master.getTelegramId())
                .active(master.getActive())
                .imageUrl(master.getImageUrl())
                .personalData(personalMapper.toEntity(master.getPersonalData()))
                .address(addressMapper.toEntity(master.getAddress()))
                .build();
    }

    public Master toEntity(MasterDto master) {
        return toEntity(master, null);

    }
}
