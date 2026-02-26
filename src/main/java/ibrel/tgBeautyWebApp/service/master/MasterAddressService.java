package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.master.MasterAddress;

public interface MasterAddressService {

    MasterAddress create(Long masterId, MasterAddress address);

    MasterAddress update(Long masterId, MasterAddress address);

    MasterAddress getByMasterId(Long masterId);

    void deleteByMasterId(Long masterId);
}
