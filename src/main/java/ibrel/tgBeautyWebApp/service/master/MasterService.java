package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.*;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;

import java.util.List;

public interface MasterService {
    Master create(Master master);
    Master update(Long id, Master master);
    Master getById(Long id);
    List<Master> getAll();
    void delete(Long id);
    Master activate(Long id);
    Master deactivate(Long id);

    // Delegation
    MasterAddress updateAddress(Long masterId, MasterAddress address);
    MasterPersonalData updatePersonalData(Long masterId, MasterPersonalData personalData);

    MasterServiceWork addServiceToMaster(Long masterId, MasterServiceWork service);
    void removeServiceFromMaster(Long masterId, Long serviceId);
    List<MasterServiceWork> getServices(Long masterId);

    WorkSlot addWorkSlot(Long masterId, WorkSlot slot);
    void removeWorkSlot(Long masterId, Long slotId);
    List<WorkSlot> getSlots(Long masterId);

    MasterReview addReview(Long masterId, MasterReview review);
    List<MasterReview> getReviews(Long masterId);
    double getAverageRating(Long masterId);

    MasterWorkExample addWorkExample(Long masterId, MasterWorkExample example);
    List<MasterWorkExample> getWorks(Long masterId);

    List<Appointment> getAppointments(Long masterId);
}
