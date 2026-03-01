package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.MasterDto;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import org.springframework.stereotype.Component;

@Component
public class MasterMapper {

    public MasterDto toDto(Master master) {
        if (master == null) return null;

        MasterPersonalData pd = master.getPersonalData();
        MasterAddress addr = master.getAddress();

        return MasterDto.builder()
                .id(master.getId())
                .active(master.getActive())
                .firstName(pd != null ? pd.getFirstName() : null)
                .lastName(pd != null ? pd.getLastName() : null)
                .description(pd != null ? pd.getDescription() : null)
                .phone(pd != null ? pd.getPhone() : null)
                .experienceYears(pd != null ? pd.getExperienceYears() : null)
                .completedJobs(pd != null ? pd.getCompletedJobs() : null)
                .city(addr != null ? addr.getCity() : null)
                .street(addr != null ? addr.getStreet() : null)
                .house(addr != null ? addr.getHouse() : null)
                .floor(addr != null ? addr.getFloor() : null)
                .apartment(addr != null ? addr.getApartment() : null)
                .placeOnTheMap(addr != null ? addr.getPlaceOnTheMap() : null)
                .build();
    }

    public Master toEntity(MasterDto dto) {
        if (dto == null) return null;

        Master master = new Master();
        master.setId(dto.getId());
        master.setActive(dto.getActive());

        MasterPersonalData pd = new MasterPersonalData();
        pd.setFirstName(dto.getFirstName());
        pd.setLastName(dto.getLastName());
        pd.setDescription(dto.getDescription());
        pd.setPhone(dto.getPhone());
        pd.setExperienceYears(dto.getExperienceYears());
        pd.setCompletedJobs(dto.getCompletedJobs());
        master.setPersonalData(pd);

        MasterAddress addr = new MasterAddress();
        addr.setCity(dto.getCity());
        addr.setStreet(dto.getStreet());
        addr.setHouse(dto.getHouse());
        addr.setFloor(dto.getFloor());
        addr.setApartment(dto.getApartment());
        addr.setPlaceOnTheMap(dto.getPlaceOnTheMap());
        master.setAddress(addr);

        return master;
    }
}
