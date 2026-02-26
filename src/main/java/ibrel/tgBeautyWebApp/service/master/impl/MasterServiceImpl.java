package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.*;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.repository.*;
import ibrel.tgBeautyWebApp.service.master.MasterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterServiceImpl implements MasterService {

    private final MasterRepository masterRepository;
    private final MasterAddressRepository addressRepository;
    private final MasterPersonalDataRepository personalDataRepository;
    private final MasterServiceWorkRepository masterServiceWorkRepository;
    private final WorkSlotRepository workSlotRepository;
    private final MasterReviewRepository reviewRepository;
    private final MasterWorkExampleRepository workExampleRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public Master create(Master master) {
        Assert.notNull(master, "Master must not be null");
        master.setCreatedAt(LocalDateTime.now());
        Master saved = masterRepository.save(master);
        log.info("Created master id={}, active={}", saved.getId(), saved.getActive());
        return saved;
    }

    @Override
    @Transactional
    public Master update(Long id, Master updated) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(updated, "Updated master must not be null");

        Master existing = masterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + id));

        // Обновляем только безопасные поля
        existing.setUpdatedAt(LocalDateTime.now());
        if (updated.getActive() != null) existing.setActive(updated.getActive());

        // Не перезаписываем коллекции целиком — для этого есть отдельные методы
        MasterPersonalData pd = existing.getPersonalData();
        if (pd != null && updated.getPersonalData() != null) {
            // частичное обновление личных данных
            MasterPersonalData updPd = updated.getPersonalData();
            pd.setFirstName(updPd.getFirstName());
            pd.setLastName(updPd.getLastName());
            pd.setDescription(updPd.getDescription());
            pd.setPhone(updPd.getPhone());
            pd.setExperienceYears(updPd.getExperienceYears());
            pd.setCompletedJobs(updPd.getCompletedJobs());
            personalDataRepository.save(pd);
        }

        Master saved = masterRepository.save(existing);
        log.info("Updated master id={}", saved.getId());
        return saved;
    }

    @Override
    public Master getById(Long id) {
        Assert.notNull(id, "Id must not be null");
        return masterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + id));
    }

    @Override
    public List<Master> getAll() {
        List<Master> list = masterRepository.findAll();
        log.debug("getAll masters count={}", list.size());
        return list;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Assert.notNull(id, "Id must not be null");
        if (!masterRepository.existsById(id)) {
            throw new EntityNotFoundException("Master not found id=" + id);
        }
        masterRepository.deleteById(id);
        log.info("Deleted master id={}", id);
    }

    @Override
    @Transactional
    public Master activate(Long id) {
        Master m = getById(id);
        if (Boolean.TRUE.equals(m.getActive())) {
            log.debug("Master id={} already active", id);
            return m;
        }
        m.setActive(true);
        m.setUpdatedAt(LocalDateTime.now());
        Master saved = masterRepository.save(m);
        log.info("Activated master id={}", id);
        return saved;
    }

    @Override
    @Transactional
    public Master deactivate(Long id) {
        Master m = getById(id);
        if (Boolean.FALSE.equals(m.getActive())) {
            log.debug("Master id={} already inactive", id);
            return m;
        }
        m.setActive(false);
        m.setUpdatedAt(LocalDateTime.now());
        Master saved = masterRepository.save(m);
        log.info("Deactivated master id={}", id);
        return saved;
    }

    @Override
    @Transactional
    public MasterAddress updateAddress(Long masterId, MasterAddress address) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(address, "address must not be null");

        Master master = getById(masterId);

        MasterAddress existing = master.getAddress();
        if (existing == null) {
            address.setMaster(master);
            MasterAddress saved = addressRepository.save(address);
            master.setAddress(saved);
            masterRepository.save(master);
            log.info("Added address for master id={}", masterId);
            return saved;
        } else {
            existing.setCity(address.getCity());
            existing.setStreet(address.getStreet());
            existing.setHouse(address.getHouse());
            existing.setFloor(address.getFloor());
            existing.setApartment(address.getApartment());
            existing.setPlaceOnTheMap(address.getPlaceOnTheMap());
            MasterAddress saved = addressRepository.save(existing);
            log.info("Updated address for master id={}", masterId);
            return saved;
        }
    }

    @Override
    @Transactional
    public MasterPersonalData updatePersonalData(Long masterId, MasterPersonalData personalData) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(personalData, "personalData must not be null");

        Master master = getById(masterId);

        MasterPersonalData existing = master.getPersonalData();
        if (existing == null) {
            personalData.setMaster(master);
            MasterPersonalData saved = personalDataRepository.save(personalData);
            master.setPersonalData(saved);
            masterRepository.save(master);
            log.info("Added personal data for master id={}", masterId);
            return saved;
        } else {
            existing.setFirstName(personalData.getFirstName());
            existing.setLastName(personalData.getLastName());
            existing.setInstUserId(personalData.getInstUserId());
            existing.setDescription(personalData.getDescription());
            existing.setPhone(personalData.getPhone());
            existing.setExperienceYears(personalData.getExperienceYears());
            existing.setCompletedJobs(personalData.getCompletedJobs());
            MasterPersonalData saved = personalDataRepository.save(existing);
            log.info("Updated personal data for master id={}", masterId);
            return saved;
        }
    }

    @Override
    @Transactional
    public MasterServiceWork addServiceToMaster(Long masterId, MasterServiceWork service) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(service, "service must not be null");

        Master master = getById(masterId);
        service.setMaster(master);
        MasterServiceWork saved = masterServiceWorkRepository.save(service);
        log.info("Added service id={} to master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public void removeServiceFromMaster(Long masterId, Long serviceId) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(serviceId, "serviceId must not be null");

        Master master = getById(masterId);
        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        if (!service.getMaster().getId().equals(master.getId())) {
            log.warn("Attempt to remove service id={} not belonging to master id={}", serviceId, masterId);
            throw new IllegalArgumentException("Service does not belong to master");
        }

        masterServiceWorkRepository.deleteById(serviceId);
        log.info("Removed service id={} from master id={}", serviceId, masterId);
    }

    @Override
    @Transactional
    public WorkSlot addWorkSlot(Long masterId, WorkSlot slot) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(slot, "slot must not be null");

        Master master = getById(masterId);
        slot.setMaster(master);

        // Проверка пересечения слотов
        List<WorkSlot> existing = workSlotRepository.findByMasterId(masterId);
        for (WorkSlot s : existing) {
            boolean overlap = !(slot.getEndTime().isBefore(s.getStartTime()) || slot.getStartTime().isAfter(s.getEndTime()));
            if (overlap) {
                log.warn("New slot overlaps with existing slot id={} for master id={}", s.getId(), masterId);
                throw new IllegalArgumentException("New slot overlaps with existing slot");
            }
        }

        WorkSlot saved = workSlotRepository.save(slot);
        log.info("Added work slot id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public void removeWorkSlot(Long masterId, Long slotId) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(slotId, "slotId must not be null");

        WorkSlot slot = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + slotId));

        if (!slot.getMaster().getId().equals(masterId)) {
            log.warn("Attempt to remove slot id={} not belonging to master id={}", slotId, masterId);
            throw new IllegalArgumentException("Slot does not belong to master");
        }

        // Проверка: есть ли записи в этом слоте
        boolean hasAppointments = appointmentRepository.findByMasterId(masterId).stream()
                .anyMatch(a -> a.getSlot() != null && a.getSlot().getId().equals(slotId));
        if (hasAppointments) {
            log.warn("Attempt to remove slot id={} with existing appointments for master id={}", slotId, masterId);
            throw new IllegalStateException("Cannot remove slot with existing appointments");
        }

        workSlotRepository.deleteById(slotId);
        log.info("Removed work slot id={} for master id={}", slotId, masterId);
    }

    @Override
    @Transactional
    public MasterReview addReview(Long masterId, MasterReview review) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(review, "review must not be null");

        Master master = getById(masterId);
        review.setMaster(master);
        MasterReview saved = reviewRepository.save(review);
        log.info("Added review id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public MasterWorkExample addWorkExample(Long masterId, MasterWorkExample example) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(example, "example must not be null");

        Master master = getById(masterId);
        example.setMaster(master);
        MasterWorkExample saved = workExampleRepository.save(example);
        log.info("Added work example id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    public List<WorkSlot> getSlots(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        return workSlotRepository.findByMasterId(masterId);
    }

    @Override
    public List<MasterServiceWork> getServices(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        return masterServiceWorkRepository.findByMasterId(masterId);
    }

    @Override
    public List<MasterReview> getReviews(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        return reviewRepository.findByMasterId(masterId);
    }

    @Override
    public List<MasterWorkExample> getWorks(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        return workExampleRepository.findByMasterId(masterId);
    }

    @Override
    public List<Appointment> getAppointments(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        return appointmentRepository.findByMasterId(masterId);
    }
}
