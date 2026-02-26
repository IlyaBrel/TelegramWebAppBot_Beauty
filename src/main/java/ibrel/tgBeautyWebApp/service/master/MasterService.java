package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.*;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;

import java.util.List;

public interface MasterService {

    Master create(Master master);

    Master update(Long id, Master updated);

    Master getById(Long id);

    List<Master> getAll();

    void delete(Long id);

    Master activate(Long id);

    Master deactivate(Long id);

    MasterAddress updateAddress(Long masterId, MasterAddress address);

    MasterPersonalData updatePersonalData(Long masterId, MasterPersonalData personalData);

    MasterServiceWork addServiceToMaster(Long masterId, MasterServiceWork service);

    void removeServiceFromMaster(Long masterId, Long serviceId);

    WorkSlot addWorkSlot(Long masterId, WorkSlot slot);

    void removeWorkSlot(Long masterId, Long slotId);

    MasterReview addReview(Long masterId, MasterReview review);

    MasterWorkExample addWorkExample(Long masterId, MasterWorkExample example);

    List<WorkSlot> getSlots(Long masterId);

    List<MasterServiceWork> getServices(Long masterId);

    List<MasterReview> getReviews(Long masterId);

    List<MasterWorkExample> getWorks(Long masterId);

    List<Appointment> getAppointments(Long masterId);
}
