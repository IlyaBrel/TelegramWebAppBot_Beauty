package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.*;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.service.master.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterServiceImpl implements MasterService {

    private final MasterRepository masterRepository;

    private final MasterAddressService addressService;
    private final MasterPersonalDataService personalDataService;
    private final MasterServiceWorkService serviceWorkService;
    private final WorkSlotService slotService;
    private final MasterReviewService reviewService;
    private final MasterWorkExampleService workExampleService;

    @Override
    @Transactional
    public Master create(Master master) {
        Assert.notNull(master, "Master must not be null");
        master.setCreatedAt(OffsetDateTime.now());
        Master saved = masterRepository.save(master);
        log.info("Created master id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Master update(Long id, Master updated) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(updated, "Updated master must not be null");
        Master existing = getById(id);

        // owner check placeholder: verify current user can update this master

        if (updated.getActive() != null) existing.setActive(updated.getActive());
        if (updated.getImageUrl() != null) existing.setImageUrl(updated.getImageUrl());
        existing.setUpdatedAt(OffsetDateTime.now());
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
        return masterRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Assert.notNull(id, "Id must not be null");
        if (!masterRepository.existsById(id)) throw new EntityNotFoundException("Master not found id=" + id);
        masterRepository.deleteById(id);
        log.info("Deleted master id={}", id);
    }

    @Override
    @Transactional
    public Master activate(Long id) {
        Master m = getById(id);
        if (!Boolean.TRUE.equals(m.getActive())) {
            m.setActive(true);
            m.setUpdatedAt(OffsetDateTime.now());
            masterRepository.save(m);
        }
        return m;
    }

    @Override
    @Transactional
    public Master deactivate(Long id) {
        Master m = getById(id);
        if (!Boolean.FALSE.equals(m.getActive())) {
            m.setActive(false);
            m.setUpdatedAt(OffsetDateTime.now());
            masterRepository.save(m);
        }
        return m;
    }

    // Delegation methods

    @Override
    public MasterAddress updateAddress(Long masterId, MasterAddress address) {
        return addressService.update(masterId, address);
    }

    @Override
    public MasterPersonalData updatePersonalData(Long masterId, MasterPersonalData personalData) {
        return personalDataService.update(masterId, personalData);
    }

    @Override
    public MasterServiceWork addServiceToMaster(Long masterId, MasterServiceWork service) {
        return serviceWorkService.create(masterId, service);
    }

    @Override
    public void removeServiceFromMaster(Long masterId, Long serviceId) {
        serviceWorkService.delete(serviceId);
    }

    @Override
    public List<MasterServiceWork> getServices(Long masterId) {
        return serviceWorkService.getByMaster(masterId);
    }

    @Override
    public WorkSlot addWorkSlot(Long masterId, WorkSlot slot) {
        return slotService.create(masterId, slot);
    }

    @Override
    public void removeWorkSlot(Long masterId, Long slotId) {
        slotService.delete(slotId);
    }

    @Override
    public List<WorkSlot> getSlots(Long masterId) {
        return slotService.findByMaster(masterId);
    }

    @Override
    public MasterReview addReview(Long masterId, MasterReview review) {
        return reviewService.addReview(masterId, review);
    }

    @Override
    public List<MasterReview> getReviews(Long masterId) {
        return reviewService.getByMaster(masterId);
    }

    @Override
    public double getAverageRating(Long masterId) {
        return reviewService.getAverageRating(masterId);
    }

    @Override
    public MasterWorkExample addWorkExample(Long masterId, MasterWorkExample example) {
        return workExampleService.add(masterId, example);
    }

    @Override
    public List<MasterWorkExample> getWorks(Long masterId) {
        return workExampleService.getByMaster(masterId);
    }

    @Override
    public List<Appointment> getAppointments(Long masterId) {
        Master m = getById(masterId);
        return m.getAppointments();
    }
}
