// src/main/java/ibrel/tgBeautyWebApp/service/master/impl/MasterServiceWorkServiceImpl.java
package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.MasterServiceWorkRepository;
import ibrel.tgBeautyWebApp.repository.VariableServiceDetailsRepository;
import ibrel.tgBeautyWebApp.service.master.MasterServiceWorkService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterServiceWorkServiceImpl implements MasterServiceWorkService {

    private final MasterRepository masterRepository;
    private final MasterServiceWorkRepository masterServiceWorkRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public MasterServiceWork create(Long masterId, MasterServiceWork service) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(service, "service must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        if (service.getName() == null || service.getName().isBlank()) {
            throw new IllegalArgumentException("Service name is required");
        }
        if (service.getType() == null) {
            throw new IllegalArgumentException("Service type is required");
        }

        switch (service.getType()) {
            case FIXED -> {
                FixedServiceDetails fd = service.getFixedDetails();
                if (fd == null) throw new IllegalArgumentException("Fixed service must contain fixedDetails");
                if (fd.getDurationMinutes() == null || fd.getDurationMinutes() <= 0)
                    throw new IllegalArgumentException("Invalid duration for fixed service");
                if (fd.getPrice() == null || fd.getPrice() < 0)
                    throw new IllegalArgumentException("Invalid price for fixed service");
            }
            case VARIABLE -> {
                List<VariableServiceDetails> vars = service.getVariableDetails();
                if (vars == null || vars.isEmpty())
                    throw new IllegalArgumentException("Variable service must contain at least one variable detail");
                for (VariableServiceDetails v : vars) {
                    if (v.getFactorName() == null || v.getFactorValue() == null)
                        throw new IllegalArgumentException("Variable detail must have factorName and factorValue");
                    if (v.getDurationMinutes() == null || v.getDurationMinutes() <= 0)
                        throw new IllegalArgumentException("Invalid duration for variable detail");
                    if (v.getPrice() == null || v.getPrice() < 0)
                        throw new IllegalArgumentException("Invalid price for variable detail");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported service type");
        }

        service.setMaster(master);
        MasterServiceWork saved = masterServiceWorkRepository.save(service);
        log.info("Created service id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public MasterServiceWork update(Long serviceId, MasterServiceWork service) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(service, "service must not be null");

        MasterServiceWork existing = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        if (service.getName() != null) existing.setName(service.getName());
        if (service.getDescription() != null) existing.setDescription(service.getDescription());
        if (service.getType() != null) existing.setType(service.getType());

        if (service.getFixedDetails() != null) {
            FixedServiceDetails fd = service.getFixedDetails();
            if (fd.getDurationMinutes() == null || fd.getDurationMinutes() <= 0)
                throw new IllegalArgumentException("Invalid duration for fixed details");
            if (fd.getPrice() == null || fd.getPrice() < 0)
                throw new IllegalArgumentException("Invalid price for fixed details");
            existing.setFixedDetails(fd);
        }

        if (service.getVariableDetails() != null) {
            for (VariableServiceDetails v : service.getVariableDetails()) {
                if (v.getFactorName() == null || v.getFactorValue() == null)
                    throw new IllegalArgumentException("Variable detail must have factorName and factorValue");
                if (v.getDurationMinutes() == null || v.getDurationMinutes() <= 0)
                    throw new IllegalArgumentException("Invalid duration for variable detail");
                if (v.getPrice() == null || v.getPrice() < 0)
                    throw new IllegalArgumentException("Invalid price for variable detail");
            }
            existing.setVariableDetails(service.getVariableDetails());
        }

        MasterServiceWork saved = masterServiceWorkRepository.save(existing);
        log.info("Updated service id={}", saved.getId());
        return saved;
    }

    @Override
    public MasterServiceWork getById(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");
        return masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));
    }

    @Override
    public List<MasterServiceWork> getByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId)) throw new EntityNotFoundException("Master not found id=" + masterId);
        return masterServiceWorkRepository.findByMasterId(masterId);
    }

    @Override
    @Transactional
    public void delete(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");
        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        boolean used = appointmentRepository.findAll().stream()
                .anyMatch(a -> a.getServices() != null && a.getServices().stream().anyMatch(s -> s.getId().equals(serviceId)));
        if (used) {
            log.warn("Attempt to delete service id={} used in appointments", serviceId);
            throw new IllegalStateException("Service is used in existing appointments and cannot be deleted");
        }

        masterServiceWorkRepository.deleteById(serviceId);
        log.info("Deleted service id={}", serviceId);
    }

    @Override
    @Transactional
    public FixedServiceDetails addOrUpdateFixedDetails(Long serviceId, FixedServiceDetails fixedDetails) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(fixedDetails, "fixedDetails must not be null");

        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        if (fixedDetails.getDurationMinutes() == null || fixedDetails.getDurationMinutes() <= 0)
            throw new IllegalArgumentException("Invalid duration for fixed details");
        if (fixedDetails.getPrice() == null || fixedDetails.getPrice() < 0)
            throw new IllegalArgumentException("Invalid price for fixed details");

        service.setFixedDetails(fixedDetails);
        MasterServiceWork saved = masterServiceWorkRepository.save(service);
        log.info("Added/updated fixed details for service id={}", serviceId);
        return saved.getFixedDetails();
    }

    @Override
    @Transactional
    public void removeFixedDetails(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");
        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        boolean used = appointmentRepository.findAll().stream()
                .anyMatch(a -> a.getServices() != null && a.getServices().stream().anyMatch(s -> s.getId().equals(serviceId)));
        if (used) {
            log.warn("Attempt to remove fixed details for service id={} used in appointments", serviceId);
            throw new IllegalStateException("Cannot remove fixed details: service used in appointments");
        }

        service.setFixedDetails(null);
        masterServiceWorkRepository.save(service);
        log.info("Removed fixed details for service id={}", serviceId);
    }

    @Override
    @Transactional
    public VariableServiceDetails addVariableDetail(Long serviceId, VariableServiceDetails variableDetail) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(variableDetail, "variableDetail must not be null");

        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        List<VariableServiceDetails> existing = variableServiceDetailsRepository.findByServiceId(serviceId);
        Optional<VariableServiceDetails> dup = existing.stream()
                .filter(v -> v.getFactorName().equalsIgnoreCase(variableDetail.getFactorName())
                        && v.getFactorValue().equalsIgnoreCase(variableDetail.getFactorValue()))
                .findAny();
        if (dup.isPresent()) {
            throw new IllegalArgumentException("Variable detail with same factorName and factorValue already exists");
        }

        if (variableDetail.getDurationMinutes() == null || variableDetail.getDurationMinutes() <= 0)
            throw new IllegalArgumentException("Invalid duration for variable detail");
        if (variableDetail.getPrice() == null || variableDetail.getPrice() < 0)
            throw new IllegalArgumentException("Invalid price for variable detail");

        variableDetail.setService(service);
        VariableServiceDetails saved = variableServiceDetailsRepository.save(variableDetail);
        log.info("Added variable detail id={} for service id={}", saved.getId(), serviceId);
        return saved;
    }

    @Override
    @Transactional
    public VariableServiceDetails updateVariableDetail(Long variableDetailId, VariableServiceDetails variableDetail) {
        Assert.notNull(variableDetailId, "variableDetailId must not be null");
        Assert.notNull(variableDetail, "variableDetail must not be null");

        VariableServiceDetails existing = variableServiceDetailsRepository.findById(variableDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + variableDetailId));

        if (variableDetail.getFactorName() != null) existing.setFactorName(variableDetail.getFactorName());
        if (variableDetail.getFactorValue() != null) existing.setFactorValue(variableDetail.getFactorValue());
        if (variableDetail.getDurationMinutes() != null) {
            if (variableDetail.getDurationMinutes() <= 0) throw new IllegalArgumentException("Invalid duration");
            existing.setDurationMinutes(variableDetail.getDurationMinutes());
        }
        if (variableDetail.getPrice() != null) {
            if (variableDetail.getPrice() < 0) throw new IllegalArgumentException("Invalid price");
            existing.setPrice(variableDetail.getPrice());
        }
        if (variableDetail.getDescription() != null) existing.setDescription(variableDetail.getDescription());

        VariableServiceDetails saved = variableServiceDetailsRepository.save(existing);
        log.info("Updated variable detail id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void removeVariableDetail(Long variableDetailId) {
        Assert.notNull(variableDetailId, "variableDetailId must not be null");
        VariableServiceDetails existing = variableServiceDetailsRepository.findById(variableDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + variableDetailId));

        boolean used = appointmentRepository.findAll().stream()
                .anyMatch(a -> a.getServices() != null && a.getServices().stream()
                        .anyMatch(s -> s.getVariableDetails() != null && s.getVariableDetails().stream()
                                .anyMatch(v -> v.getId().equals(variableDetailId))));
        if (used) {
            log.warn("Attempt to delete variable detail id={} used in appointments", variableDetailId);
            throw new IllegalStateException("Variable detail is used in existing appointments and cannot be deleted");
        }

        variableServiceDetailsRepository.deleteById(variableDetailId);
        log.info("Deleted variable detail id={}", variableDetailId);
    }
}
