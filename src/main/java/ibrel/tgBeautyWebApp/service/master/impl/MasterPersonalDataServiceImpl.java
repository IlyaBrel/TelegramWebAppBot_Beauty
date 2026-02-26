package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import ibrel.tgBeautyWebApp.repository.MasterPersonalDataRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.service.master.MasterPersonalDataService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterPersonalDataServiceImpl implements MasterPersonalDataService {

    private final MasterRepository masterRepository;
    private final MasterPersonalDataRepository personalDataRepository;

    @Override
    @Transactional
    public MasterPersonalData create(Long masterId, MasterPersonalData personalData) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(personalData, "personalData must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        if (master.getPersonalData() != null) {
            log.warn("Attempt to create personal data for master id={} which already has personalData id={}",
                    masterId, master.getPersonalData().getId());
            throw new IllegalStateException("Personal data already exists for master");
        }

        validatePersonalData(personalData);

        personalData.setMaster(master);
        MasterPersonalData saved = personalDataRepository.save(personalData);
        master.setPersonalData(saved);
        masterRepository.save(master);

        log.info("Created personal data id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public MasterPersonalData update(Long masterId, MasterPersonalData personalData) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(personalData, "personalData must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        MasterPersonalData existing = master.getPersonalData();
        if (existing == null) {
            // create if absent
            personalData.setMaster(master);
            validatePersonalData(personalData);
            MasterPersonalData saved = personalDataRepository.save(personalData);
            master.setPersonalData(saved);
            masterRepository.save(master);
            log.info("Added personal data id={} for master id={}", saved.getId(), masterId);
            return saved;
        }

        // partial update: null fields are ignored
        if (personalData.getFirstName() != null) existing.setFirstName(personalData.getFirstName());
        if (personalData.getLastName() != null) existing.setLastName(personalData.getLastName());
        if (personalData.getInstUserId() != null) existing.setInstUserId(personalData.getInstUserId());
        if (personalData.getDescription() != null) existing.setDescription(personalData.getDescription());
        if (personalData.getPhone() != null) existing.setPhone(personalData.getPhone());
        if (personalData.getExperienceYears() != null) existing.setExperienceYears(personalData.getExperienceYears());
        if (personalData.getCompletedJobs() != null) existing.setCompletedJobs(personalData.getCompletedJobs());

        validatePersonalData(existing);

        MasterPersonalData saved = personalDataRepository.save(existing);
        log.info("Updated personal data id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    public MasterPersonalData getByMasterId(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        MasterPersonalData pd = master.getPersonalData();
        if (pd == null) throw new EntityNotFoundException("Personal data not found for master id=" + masterId);
        return pd;
    }

    @Override
    @Transactional
    public void deleteByMasterId(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        MasterPersonalData pd = master.getPersonalData();
        if (pd == null) {
            log.debug("No personal data to delete for master id={}", masterId);
            return;
        }

        master.setPersonalData(null);
        masterRepository.save(master);
        personalDataRepository.deleteById(pd.getId());
        log.info("Deleted personal data id={} for master id={}", pd.getId(), masterId);
    }

    // базовая валидация полей личных данных
    private void validatePersonalData(MasterPersonalData pd) {
        if (pd.getFirstName() != null && pd.getFirstName().length() > 100) {
            throw new IllegalArgumentException("firstName length must be <= 100");
        }
        if (pd.getLastName() != null && pd.getLastName().length() > 100) {
            throw new IllegalArgumentException("lastName length must be <= 100");
        }
        if (pd.getInstUserId() != null && pd.getInstUserId().length() > 100) {
            throw new IllegalArgumentException("instUserId length must be <= 100");
        }
        if (pd.getDescription() != null && pd.getDescription().length() > 2000) {
            throw new IllegalArgumentException("description length must be <= 2000");
        }
        if (pd.getPhone() != null && pd.getPhone().length() > 50) {
            throw new IllegalArgumentException("phone length must be <= 50");
        }
        if (pd.getExperienceYears() != null && pd.getExperienceYears() < 0) {
            throw new IllegalArgumentException("experienceYears must be >= 0");
        }
        if (pd.getCompletedJobs() != null && pd.getCompletedJobs() < 0) {
            throw new IllegalArgumentException("completedJobs must be >= 0");
        }
    }
}
