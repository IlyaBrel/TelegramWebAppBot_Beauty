package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.*;
import ibrel.tgBeautyWebApp.service.master.MasterServiceWorkService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterServiceEntityServiceImpl implements MasterServiceWorkService {

    private final MasterRepository masterRepository;
    private final MasterServiceWorkRepository masterServiceRepository;
    private final FixedServiceDetailsRepository fixedServiceDetailsRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;
    private final AppointmentRepository appointmentRepository;

    // create: привязать услугу к мастеру, валидировать имя и fixedDetails при FIXED
    @Override
    @Transactional
    public MasterServiceWork create(Long masterId, MasterServiceWork service) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(service, "service must not be null");

        Master master = masterRepository.findById(masterId).orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        if (service.getName() == null || service.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name must not be empty");
        }

        service.setMaster(master);

        if (service.getType() != null && service.getType().name().equals("FIXED")) {
            FixedServiceDetails fd = service.getFixedDetails();
            if (fd == null) throw new IllegalArgumentException("Fixed service must contain fixedDetails");
            validateFixedDetails(fd);
            fd.setService(service);
        }

        MasterServiceWork saved = masterServiceRepository.save(service);
        log.info("Created master service id={} masterId={} name={}", saved.getId(), masterId, saved.getName());
        return saved;
    }

    // update: частичное обновление полей, управление fixedDetails если переданы
    @Override
    @Transactional
    public MasterServiceWork update(Long serviceId, MasterServiceWork service) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(service, "service must not be null");

        MasterServiceWork existing = masterServiceRepository.findById(serviceId).orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        if (service.getName() != null) existing.setName(service.getName());
        if (service.getDescription() != null) existing.setDescription(service.getDescription());
        if (service.getType() != null) existing.setType(service.getType());

        if (existing.getType() != null && existing.getType().name().equals("FIXED") && service.getFixedDetails() != null) {
            FixedServiceDetails fd = service.getFixedDetails();
            validateFixedDetails(fd);
            FixedServiceDetails existingFd = existing.getFixedDetails();
            if (existingFd == null) {
                fd.setService(existing);
                fixedServiceDetailsRepository.save(fd);
                existing.setFixedDetails(fd);
            } else {
                existingFd.setPrice(fd.getPrice());
                existingFd.setDurationMinutes(fd.getDurationMinutes());
                fixedServiceDetailsRepository.save(existingFd);
            }
        }

        MasterServiceWork saved = masterServiceRepository.save(existing);
        log.info("Updated master service id={}", saved.getId());
        return saved;
    }

    // getById
    @Override
    public MasterServiceWork getById(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");
        return masterServiceRepository.findById(serviceId).orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));
    }

    // getByMaster
    @Override
    public List<MasterServiceWork> getByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        masterRepository.findById(masterId).orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));
        return masterServiceRepository.findByMasterId(masterId);
    }

    // delete: запрет удаления, если услуга используется в записях; удаление деталей
    @Override
    @Transactional
    public void delete(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");
        MasterServiceWork service = masterServiceRepository.findById(serviceId).orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        List<Appointment> usingAppointments = appointmentRepository.findByMasterId(service.getMaster().getId()).stream().filter(a -> a.getServices() != null).filter(a -> a.getServices().stream().anyMatch(s -> s.getId().equals(serviceId))).collect(Collectors.toList());

        if (!usingAppointments.isEmpty()) {
            log.warn("Attempt to delete service id={} used in {} appointments", serviceId, usingAppointments.size());
            throw new IllegalStateException("Cannot delete service used in existing appointments");
        }

        if (service.getFixedDetails() != null) {
            fixedServiceDetailsRepository.deleteById(service.getFixedDetails().getId());
        }
        if (service.getVariableDetails() != null && !service.getVariableDetails().isEmpty()) {
            for (VariableServiceDetails vd : service.getVariableDetails()) {
                variableServiceDetailsRepository.deleteById(vd.getId());
            }
        }

        masterServiceRepository.deleteById(serviceId);
        log.info("Deleted master service id={}", serviceId);
    }

    // addOrUpdateFixedDetails: валидация и привязка
    @Override
    @Transactional
    public FixedServiceDetails addOrUpdateFixedDetails(Long serviceId, FixedServiceDetails fixedDetails) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(fixedDetails, "fixedDetails must not be null");
        validateFixedDetails(fixedDetails);

        MasterServiceWork service = masterServiceRepository.findById(serviceId).orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        FixedServiceDetails existing = service.getFixedDetails();
        if (existing == null) {
            fixedDetails.setService(service);
            FixedServiceDetails saved = fixedServiceDetailsRepository.save(fixedDetails);
            service.setFixedDetails(saved);
            masterServiceRepository.save(service);
            log.info("Added fixed details for service id={}", serviceId);
            return saved;
        } else {
            existing.setPrice(fixedDetails.getPrice());
            existing.setDurationMinutes(fixedDetails.getDurationMinutes());
            FixedServiceDetails saved = fixedServiceDetailsRepository.save(existing);
            log.info("Updated fixed details for service id={}", serviceId);
            return saved;
        }
    }

    // removeFixedDetails: проверка использования в записях
    @Override
    @Transactional
    public void removeFixedDetails(Long serviceId) {
        Assert.notNull(serviceId, "serviceId must not be null");
        MasterServiceWork service = masterServiceRepository.findById(serviceId).orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        FixedServiceDetails fd = service.getFixedDetails();
        if (fd == null) {
            log.debug("No fixed details to remove for service id={}", serviceId);
            return;
        }

        List<Appointment> usingAppointments = appointmentRepository.findByMasterId(service.getMaster().getId()).stream().filter(a -> a.getServices() != null).filter(a -> a.getServices().stream().anyMatch(s -> s.getId().equals(serviceId))).collect(Collectors.toList());

        if (!usingAppointments.isEmpty()) {
            log.warn("Attempt to remove fixed details for service id={} used in appointments", serviceId);
            throw new IllegalStateException("Cannot remove fixed details for service used in appointments");
        }

        service.setFixedDetails(null);
        masterServiceRepository.save(service);
        fixedServiceDetailsRepository.deleteById(fd.getId());
        log.info("Removed fixed details for service id={}", serviceId);
    }

    // addVariableDetail: привязать фактор к услуге
    @Override
    @Transactional
    public VariableServiceDetails addVariableDetail(Long serviceId, VariableServiceDetails variableDetail) {
        Assert.notNull(serviceId, "serviceId must not be null");
        Assert.notNull(variableDetail, "variableDetail must not be null");

        MasterServiceWork service = masterServiceRepository.findById(serviceId).orElseThrow(() -> new EntityNotFoundException("Service not found id=" + serviceId));

        variableDetail.setService(service);
        VariableServiceDetails saved = variableServiceDetailsRepository.save(variableDetail);
        log.info("Added variable detail id={} to service id={}", saved.getId(), serviceId);
        return saved;
    }

    // updateVariableDetail: частичное обновление фактора
    @Override
    @Transactional
    public VariableServiceDetails updateVariableDetail(Long variableDetailId, VariableServiceDetails variableDetail) {
        Assert.notNull(variableDetailId, "variableDetailId must not be null");
        Assert.notNull(variableDetail, "variableDetail must not be null");

        VariableServiceDetails existing = variableServiceDetailsRepository.findById(variableDetailId).orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + variableDetailId));

        if (variableDetail.getFactorName() != null) existing.setFactorName(variableDetail.getFactorName());
        if (variableDetail.getFactorValue() != null) existing.setFactorValue(variableDetail.getFactorValue());
        if (variableDetail.getPrice() != null) {
            if (variableDetail.getPrice() < 0) throw new IllegalArgumentException("Price must be >= 0");
            existing.setPrice(variableDetail.getPrice());
        }
        if (variableDetail.getDurationMinutes() != null) {
            if (variableDetail.getDurationMinutes() <= 0) throw new IllegalArgumentException("Duration must be > 0");
            existing.setDurationMinutes(variableDetail.getDurationMinutes());
        }

        VariableServiceDetails saved = variableServiceDetailsRepository.save(existing);
        log.info("Updated variable detail id={}", saved.getId());
        return saved;
    }

    // removeVariableDetail: проверка использования в записях перед удалением
    @Override
    @Transactional
    public void removeVariableDetail(Long variableDetailId) {
        Assert.notNull(variableDetailId, "variableDetailId must not be null");
        VariableServiceDetails existing = variableServiceDetailsRepository.findById(variableDetailId).orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + variableDetailId));

        Long serviceId = existing.getService().getId();
        List<Appointment> usingAppointments = appointmentRepository.findByMasterId(existing.getService().getMaster().getId()).stream().filter(a -> a.getServices() != null).filter(a -> a.getServices().stream().anyMatch(s -> s.getId().equals(serviceId))).collect(Collectors.toList());

        if (!usingAppointments.isEmpty()) {
            log.warn("Attempt to remove variable detail id={} for service id={} used in appointments", variableDetailId, serviceId);
            throw new IllegalStateException("Cannot remove variable detail for service used in appointments");
        }

        variableServiceDetailsRepository.deleteById(variableDetailId);
        log.info("Removed variable detail id={}", variableDetailId);
    }

    // вспомогательная валидация fixedDetails
    private void validateFixedDetails(FixedServiceDetails fd) {
        Assert.notNull(fd, "fixedDetails must not be null");
        if (fd.getDurationMinutes() == null || fd.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("fixedDetails.durationMinutes must be > 0");
        }
        if (fd.getPrice() == null || fd.getPrice() < 0) {
            throw new IllegalArgumentException("fixedDetails.price must be >= 0");
        }
    }
}
