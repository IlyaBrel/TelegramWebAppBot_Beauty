package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.MasterAddressDto;
import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import org.springframework.stereotype.Component;

@Component
public class MasterAddressMapper {

    public MasterAddressDto toDto(MasterAddress a) {
        if (a == null) return null;
        return MasterAddressDto.builder()
                .id(a.getId())
                .city(a.getCity())
                .street(a.getStreet())
                .house(a.getHouse())
                .floor(a.getFloor())
                .apartment(a.getApartment())
                .placeOnTheMap(a.getPlaceOnTheMap())
                .build();
    }

    public MasterAddress toEntity(MasterAddressDto dto) {
        if (dto == null) return null;
        MasterAddress a = new MasterAddress();
        a.setId(dto.getId());
        a.setCity(dto.getCity());
        a.setStreet(dto.getStreet());
        a.setHouse(dto.getHouse());
        a.setFloor(dto.getFloor());
        a.setApartment(dto.getApartment());
        a.setPlaceOnTheMap(dto.getPlaceOnTheMap());
        return a;
    }
}
