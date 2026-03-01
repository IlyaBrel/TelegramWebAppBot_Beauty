package ibrel.tgBeautyWebApp.service.booking.impl;

import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.booking.AppointmentServiceItem;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.repository.*;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceItemRepository itemRepository;
    private final MasterRepository masterRepository;
    private final WorkSlotRepository workSlotRepository;
    private final MasterServiceWorkRepository masterServiceWorkRepository;
    private final VariableServiceDetailsRepository variableServiceDetailsRepository;

    @Override
    @Transactional
    public Appointment createAppointment(String clientId,
                                         Long masterId,
                                         Long slotId,
                                         List<ServiceSelectionDto> services) {

        Assert.hasText(clientId, "clientId must not be empty");
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(slotId, "slotId must not be null");
        Assert.notEmpty(services, "services must not be empty");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        WorkSlot slot = workSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found id=" + slotId));

        if (slot.getMaster() == null || !slot.getMaster().getId().equals(masterId)) {
            throw new IllegalArgumentException("Slot does not belong to the specified master");
        }

        if (appointmentRepository.existsBySlot_Id(slotId)) {
            throw new IllegalStateException("Slot is already booked");
        }

        int totalDuration = 0;
        double totalPrice = 0.0;

        Appointment appointment = Appointment.builder()
                .clientId(clientId)
                .master(master)
                .slot(slot)
                .status(Appointment.Status.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        List<AppointmentServiceItem> items = new ArrayList<>();

        for (ServiceSelectionDto sel : services) {
            MasterServiceWork service = masterServiceWorkRepository.findById(sel.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found id=" + sel.getServiceId()));

            if (service.getMaster() == null || !service.getMaster().getId().equals(masterId)) {
                throw new IllegalArgumentException("Service id=" + sel.getServiceId() + " does not belong to master id=" + masterId);
            }

            List<VariableServiceDetails> vars = new ArrayList<>();

            if (service.getType() != null && service.getType().name().equals("FIXED")) {
                if (service.getFixedDetails() == null) {
                    throw new IllegalArgumentException("Fixed service id=" + service.getId() + " has no fixed details");
                }
                totalDuration += Optional.ofNullable(service.getFixedDetails().getDurationMinutes()).orElse(0);
                totalPrice += Optional.ofNullable(service.getFixedDetails().getPrice()).orElse(0.0);
            } else { // VARIABLE
                if (sel.getVariableDetailIds() == null || sel.getVariableDetailIds().isEmpty()) {
                    throw new IllegalArgumentException("Variable service id=" + service.getId() + " requires variable details");
                }
                for (Long varId : sel.getVariableDetailIds()) {
                    VariableServiceDetails v = variableServiceDetailsRepository.findById(varId)
                            .orElseThrow(() -> new EntityNotFoundException("Variable detail not found id=" + varId));
                    if (v.getService() == null || !v.getService().getId().equals(service.getId())) {
                        throw new IllegalArgumentException("Variable detail id=" + varId + " does not belong to service id=" + service.getId());
                    }
                    vars.add(v);
                    totalDuration += Optional.ofNullable(v.getDurationMinutes()).orElse(0);
                    totalPrice += Optional.ofNullable(v.getPrice()).orElse(0.0);
                }
            }

            AppointmentServiceItem item = AppointmentServiceItem.builder()
                    .appointment(appointment)
                    .service(service)
                    .variableDetails(vars)
                    .build();

            items.add(item);
        }

        long slotMinutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        if (totalDuration > slotMinutes) {
            throw new IllegalArgumentException("Total duration " + totalDuration + " exceeds slot duration " + slotMinutes);
        }

        appointment.setItems(items);
        Appointment saved = appointmentRepository.save(appointment);
        items.forEach(itemRepository::save);

        log.info("Created appointment id={} masterId={} slotId={} client={} servicesCount={}",
                saved.getId(), masterId, slotId, clientId, items.size());

        return saved;
    }

    @Override
    public Appointment getById(Long id) {
        Assert.notNull(id, "id must not be null");
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found id=" + id));
    }

    @Override
    public List<Appointment> getByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId)) {
            throw new EntityNotFoundException("Master not found id=" + masterId);
        }
        return appointmentRepository.findByMaster_IdOrderByCreatedAtDesc(masterId);
    }

    @Override
    public List<Appointment> getByClient(String clientId) {
        Assert.hasText(clientId, "clientId must not be empty");
        return appointmentRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Override
    @Transactional
    public Appointment cancel(Long appointmentId, String byClientId) {
        Assert.notNull(appointmentId, "appointmentId must not be null");
        Appointment existing = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found id=" + appointmentId));

        if (byClientId != null && !byClientId.equals(existing.getClientId())) {
            // сюда можно добавить проверку ролей (мастер/админ) через SecurityContext
            throw new IllegalArgumentException("Only the client who created the appointment or authorized user can cancel it");
        }

        existing.setStatus(Appointment.Status.CANCELLED);
        existing.setUpdatedAt(OffsetDateTime.now());
        Appointment saved = appointmentRepository.save(existing);
        log.info("Cancelled appointment id={} by client={}", appointmentId, byClientId);
        return saved;
    }

    @Override
    @Transactional
    public Appointment confirm(Long appointmentId) {
        Assert.notNull(appointmentId, "appointmentId must not be null");
        Appointment existing = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found id=" + appointmentId));

        existing.setStatus(Appointment.Status.CONFIRMED);
        existing.setUpdatedAt(OffsetDateTime.now());
        Appointment saved = appointmentRepository.save(existing);
        log.info("Confirmed appointment id={}", appointmentId);
        return saved;
    }
}
