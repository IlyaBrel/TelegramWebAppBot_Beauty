package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.master.MasterWorkExample;

import java.util.List;

public interface MasterWorkExampleService {
    MasterWorkExample add(Long masterId, MasterWorkExample example);
    MasterWorkExample getById(Long id);
    List<MasterWorkExample> getByMaster(Long masterId);
    void delete(Long exampleId);
}
