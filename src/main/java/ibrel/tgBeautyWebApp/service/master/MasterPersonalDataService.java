package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;

public interface MasterPersonalDataService {
    MasterPersonalData create(Long masterId, MasterPersonalData personalData);
    MasterPersonalData update(Long masterId, MasterPersonalData personalData);
    MasterPersonalData getByMasterId(Long masterId);
    void deleteByMasterId(Long masterId);
}
