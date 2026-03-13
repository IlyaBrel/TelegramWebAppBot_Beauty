package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterRepository;
import ibrel.tgBeautyWebApp.repository.master.MasterServiceWorkRepository;
import ibrel.tgBeautyWebApp.repository.master.VariableServiceDetailsRepository;
import ibrel.tgBeautyWebApp.service.master.MasterServiceWorkService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterServiceWorkServiceImpl implements MasterServiceWorkService {

    private final MasterRepository                masterRepository;
    private final MasterServiceWorkRepository     masterServiceWorkRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;
    private final AppointmentRepository           appointmentRepository;

    @Override
    @Transactional
    public MasterServiceWork create(Long masterId, MasterServiceWork service) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(service,  "service must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        if (service.getName() == null || service.getName().isBlank())
            throw new IllegalArgumentException("Service name is required");
        if (service.getType() == null)
            throw new IllegalArgumentException("Service type is required");

        switch (service.getType()) {
            case FIXED -> {
                FixedServiceDetails fd = service.getFixedDetails();
                if (fd == null)
                    throw new IllegalArgumentException("Fixed service must contain fixedDetails");
                validateFixed(fd);
            }
            case VARIABLE -> {
                List<VariableServiceDetails> vars = service.getVariableDetails();
                if (vars == null || vars.isEmpty())
                    throw new IllegalArgumentException("Variable service must contain at least one variable detail");
                vars.forEach(this::validateVariable);
            }
        }

        service.setMaster(master);
        if (service.getActive() == null) service.setActive(true);
        MasterServiceWork saved = masterServiceWorkRepository.save(service);
        log.info("Created service id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public MasterServiceWork update(Long serviceId, MasterServiceWork service) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(service,   "service must not be null");

        MasterServiceWork existing = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        if (service.getName()        != null) existing.setName(service.getName());
        if (service.getDescription() != null) existing.setDescription(service.getDescription());
        if (service.getImageUrl()    != null) existing.setImageUrl(service.getImageUrl());
        if (service.getActive()      != null) existing.setActive(service.getActive());
        if (service.getType()        != null) existing.setType(service.getType());

        if (service.getFixedDetails() != null) {
            validateFixed(service.getFixedDetails());
            existing.setFixedDetailsBidirectional(service.getFixedDetails());
        }
        if (service.getVariableDetails() != null) {
            service.getVariableDetails().forEach(this::validateVariable);
            existing.setVariableDetails(service.getVariableDetails());
        }

        MasterServiceWork saved = masterServiceWorkRepository.save(existing);
        log.info("Updated service id={}", serviceId);
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
        if (!masterRepository.existsById(masterId))
            throw new EntityNotFoundException("Master not found id=" + masterId);
        return masterServiceWorkRepository.findByMaster_IdAndActiveTrueOrderByNameAsc(masterId);
    }

    @Override
    @Transactional
    public void delete(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");
        masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        if (appointmentRepository.existsByItems_Service_Id(serviceId)) {
            throw new IllegalStateException("Service is used in existing appointments and cannot be deleted");
        }
        masterServiceWorkRepository.deleteById(serviceId);
        log.info("Deleted service id={}", serviceId);
    }

    @Override
    @Transactional
    public FixedServiceDetails addOrUpdateFixedDetails(Long serviceId, FixedServiceDetails fd) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(fd,        "fixedDetails must not be null");
        validateFixed(fd);

        if (appointmentRepository.existsByItems_Service_Id(serviceId)) {
            throw new IllegalStateException("Cannot modify fixed details: service used in appointments");
        }

        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        service.setFixedDetailsBidirectional(fd);
        masterServiceWorkRepository.save(service);
        log.info("Updated fixed details for service id={}", serviceId);
        return service.getFixedDetails();
    }

    @Override
    @Transactional
    public void removeFixedDetails(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");

        if (appointmentRepository.existsByItems_Service_Id(serviceId)) {
            throw new IllegalStateException("Cannot remove fixed details: service used in appointments");
        }

        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        service.setFixedDetails(null);
        masterServiceWorkRepository.save(service);
        log.info("Removed fixed details for service id={}", serviceId);
    }

    @Override
    @Transactional
    public VariableServiceDetails addVariableDetail(Long serviceId, VariableServiceDetails variableDetail) {
        Assert.notNull(serviceId,     "serviceId must not be null");
        Assert.notNull(variableDetail, "variableDetail must not be null");
        validateVariable(variableDetail);

        MasterServiceWork service = masterServiceWorkRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        // Проверка дубликата
        List<VariableServiceDetails> existing =
                variableServiceDetailsRepository.findByService_IdOrderByFactorNameAsc(serviceId);
        boolean dup = existing.stream().anyMatch(v ->
                v.getFactorName().equalsIgnoreCase(variableDetail.getFactorName())
                        && v.getFactorValue().equalsIgnoreCase(variableDetail.getFactorValue()));
        if (dup) {
            throw new IllegalArgumentException(
                    "Variable detail with same factorName and factorValue already exists");
        }

        variableDetail.setService(service);
        VariableServiceDetails saved = variableServiceDetailsRepository.save(variableDetail);
        log.info("Added variable detail id={} for service id={}", saved.getId(), serviceId);
        return saved;
    }

    @Override
    @Transactional
    public VariableServiceDetails updateVariableDetail(Long variableDetailId,
                                                       VariableServiceDetails updated) {
        Assert.notNull(variableDetailId, "variableDetailId must not be null");
        Assert.notNull(updated,          "variableDetail must not be null");

        VariableServiceDetails existing = variableServiceDetailsRepository.findById(variableDetailId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Variable detail not found id=" + variableDetailId));

        if (updated.getFactorName()      != null) existing.setFactorName(updated.getFactorName());
        if (updated.getFactorValue()     != null) existing.setFactorValue(updated.getFactorValue());
        if (updated.getDescription()     != null) existing.setDescription(updated.getDescription());
        if (updated.getDurationMinutes() != null) {
            if (updated.getDurationMinutes() <= 0) throw new IllegalArgumentException("Invalid duration");
            existing.setDurationMinutes(updated.getDurationMinutes());
        }
        if (updated.getPrice() != null) {
            if (updated.getPrice() < 0) throw new IllegalArgumentException("Invalid price");
            existing.setPrice(updated.getPrice());
        }

        VariableServiceDetails saved = variableServiceDetailsRepository.save(existing);
        log.info("Updated variable detail id={}", variableDetailId);
        return saved;
    }

    @Override
    @Transactional
    public void removeVariableDetail(Long variableDetailId) {
        Assert.notNull(variableDetailId, "variableDetailId must not be null");
        variableServiceDetailsRepository.findById(variableDetailId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Variable detail not found id=" + variableDetailId));

        if (appointmentRepository.existsByItems_VariableDetails_Id(variableDetailId)) {
            throw new IllegalStateException(
                    "Variable detail is used in existing appointments and cannot be deleted");
        }

        variableServiceDetailsRepository.deleteById(variableDetailId);
        log.info("Deleted variable detail id={}", variableDetailId);
    }

    // ── Валидация ─────────────────────────────────────────────────────────────

    private void validateFixed(FixedServiceDetails fd) {
        if (fd.getDurationMinutes() == null || fd.getDurationMinutes() <= 0)
            throw new IllegalArgumentException("durationMinutes must be > 0");
        if (fd.getPrice() == null || fd.getPrice() < 0)
            throw new IllegalArgumentException("price must be >= 0");
    }

    private void validateVariable(VariableServiceDetails v) {
        if (v.getFactorName()  == null || v.getFactorName().isBlank())
            throw new IllegalArgumentException("factorName is required");
        if (v.getFactorValue() == null || v.getFactorValue().isBlank())
            throw new IllegalArgumentException("factorValue is required");
        if (v.getDurationMinutes() == null || v.getDurationMinutes() <= 0)
            throw new IllegalArgumentException("durationMinutes must be > 0");
        if (v.getPrice() == null || v.getPrice() < 0)
            throw new IllegalArgumentException("price must be >= 0");
    }
}