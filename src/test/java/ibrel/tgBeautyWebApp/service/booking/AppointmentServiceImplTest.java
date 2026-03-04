package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.booking.AppointmentServiceItem;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.enums.MasterServiceType;
import ibrel.tgBeautyWebApp.repository.*;
import ibrel.tgBeautyWebApp.service.booking.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock AppointmentServiceItemRepository itemRepository;
    @Mock MasterRepository masterRepository;
    @Mock WorkSlotRepository workSlotRepository;
    @Mock MasterServiceWorkRepository masterServiceWorkRepository;
    @Mock VariableServiceDetailsRepository variableServiceDetailsRepository;

    @InjectMocks AppointmentServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- CREATE APPOINTMENT SUCCESS ----------
    @Test
    void createAppointment_success_fixedService() {
        Long masterId = 1L;
        Long slotId = 10L;

        Master master = new Master();
        master.setId(masterId);

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);
        slot.setMaster(master);
        slot.setStartTime(LocalTime.from(LocalDateTime.now()));
        slot.setEndTime(LocalTime.from(LocalDateTime.now().plusMinutes(60)));

        MasterServiceWork serviceWork = new MasterServiceWork();
        serviceWork.setId(100L);
        serviceWork.setMaster(master);
        serviceWork.setType(MasterServiceType.FIXED);

        FixedServiceDetails fixed = new FixedServiceDetails();
        fixed.setDurationMinutes(30);
        fixed.setPrice(50.0);
        serviceWork.setFixedDetails(fixed);

        ServiceSelectionDto dto = new ServiceSelectionDto();
        dto.setServiceId(100L);

        Appointment saved = new Appointment();
        saved.setId(999L);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlot_Id(slotId)).thenReturn(false);
        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(serviceWork));
        when(appointmentRepository.save(any())).thenReturn(saved);

        Appointment result = service.createAppointment("client1", masterId, slotId, List.of(dto));

        assertEquals(999L, result.getId());
        verify(itemRepository, times(1)).save(any());
    }

    // ---------- ERRORS ----------
    @Test
    void createAppointment_masterNotFound() {
        when(masterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.createAppointment("c", 1L, 1L, List.of(new ServiceSelectionDto())));
    }

    @Test
    void createAppointment_slotNotFound() {
        Master master = new Master();
        master.setId(1L);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.createAppointment("c", 1L, 10L, List.of(new ServiceSelectionDto())));
    }

    @Test
    void createAppointment_slotBelongsToAnotherMaster() {
        Master master = new Master();
        master.setId(1L);

        Master other = new Master();
        other.setId(2L);

        WorkSlot slot = new WorkSlot();
        slot.setId(10L);
        slot.setMaster(other);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(IllegalArgumentException.class,
                () -> service.createAppointment("c", 1L, 10L, List.of(new ServiceSelectionDto())));
    }

    @Test
    void createAppointment_slotAlreadyBooked() {
        Master master = new Master();
        master.setId(1L);

        WorkSlot slot = new WorkSlot();
        slot.setId(10L);
        slot.setMaster(master);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlot_Id(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.createAppointment("c", 1L, 10L, List.of(new ServiceSelectionDto())));
    }

    // ---------- VARIABLE SERVICE ----------
    @Test
    void createAppointment_variableService_success() {
        Long masterId = 1L;
        Long slotId = 10L;

        Master master = new Master();
        master.setId(masterId);

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);
        slot.setMaster(master);
        slot.setStartTime(LocalTime.from(LocalDateTime.now()));
        slot.setEndTime(LocalTime.from(LocalDateTime.now().plusMinutes(120)));

        MasterServiceWork serviceWork = new MasterServiceWork();
        serviceWork.setId(100L);
        serviceWork.setMaster(master);
        serviceWork.setType(MasterServiceType.VARIABLE);

        VariableServiceDetails var = new VariableServiceDetails();
        var.setId(200L);
        var.setService(serviceWork);
        var.setDurationMinutes(40);
        var.setPrice(30.0);

        ServiceSelectionDto dto = new ServiceSelectionDto();
        dto.setServiceId(100L);
        dto.setVariableDetailIds(List.of(200L));

        Appointment saved = new Appointment();
        saved.setId(999L);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlot_Id(slotId)).thenReturn(false);
        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(serviceWork));
        when(variableServiceDetailsRepository.findById(200L)).thenReturn(Optional.of(var));
        when(appointmentRepository.save(any())).thenReturn(saved);

        Appointment result = service.createAppointment("client", masterId, slotId, List.of(dto));

        assertEquals(999L, result.getId());
        verify(itemRepository).save(any());
    }

    @Test
    void createAppointment_variableService_missingDetails() {
        Master master = new Master();
        master.setId(1L);

        WorkSlot slot = new WorkSlot();
        slot.setId(10L);
        slot.setMaster(master);
        slot.setStartTime(LocalTime.from(LocalDateTime.now()));
        slot.setEndTime(LocalTime.from(LocalDateTime.now().plusMinutes(60)));

        MasterServiceWork serviceWork = new MasterServiceWork();
        serviceWork.setId(100L);
        serviceWork.setMaster(master);
        serviceWork.setType(MasterServiceType.VARIABLE);

        ServiceSelectionDto dto = new ServiceSelectionDto();
        dto.setServiceId(100L);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlot_Id(10L)).thenReturn(false);
        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(serviceWork));

        assertThrows(IllegalArgumentException.class,
                () -> service.createAppointment("c", 1L, 10L, List.of(dto)));
    }

    // ---------- DURATION EXCEEDS SLOT ----------
    @Test
    void createAppointment_durationExceedsSlot() {
        Master master = new Master();
        master.setId(1L);

        WorkSlot slot = new WorkSlot();
        slot.setId(10L);
        slot.setMaster(master);
        slot.setStartTime(LocalTime.from(LocalDateTime.now()));
        slot.setEndTime(LocalTime.from(LocalDateTime.now().plusMinutes(30)));

        MasterServiceWork serviceWork = new MasterServiceWork();
        serviceWork.setId(100L);
        serviceWork.setMaster(master);
        serviceWork.setType(MasterServiceType.FIXED);

        FixedServiceDetails fixed = new FixedServiceDetails();
        fixed.setDurationMinutes(60);
        fixed.setPrice(50.0);
        serviceWork.setFixedDetails(fixed);

        ServiceSelectionDto dto = new ServiceSelectionDto();
        dto.setServiceId(100L);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlot_Id(10L)).thenReturn(false);
        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(serviceWork));

        assertThrows(IllegalArgumentException.class,
                () -> service.createAppointment("c", 1L, 10L, List.of(dto)));
    }

    // ---------- GET ----------
    @Test
    void getById_success() {
        Appointment a = new Appointment();
        a.setId(5L);

        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(a));

        Appointment result = service.getById(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void getById_notFound() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getById(5L));
    }

    @Test
    void getByMaster_success() {
        when(masterRepository.existsById(1L)).thenReturn(true);

        List<Appointment> list = List.of(new Appointment());
        when(appointmentRepository.findByMaster_IdOrderByCreatedAtDesc(1L)).thenReturn(list);

        List<Appointment> result = service.getByMaster(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getByMaster_notFound() {
        when(masterRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.getByMaster(1L));
    }

    @Test
    void getByClient_success() {
        List<Appointment> list = List.of(new Appointment());
        when(appointmentRepository.findByClientIdOrderByCreatedAtDesc("c")).thenReturn(list);

        List<Appointment> result = service.getByClient("c");

        assertEquals(1, result.size());
    }

    // ---------- CANCEL ----------
    @Test
    void cancel_success() {
        Appointment a = new Appointment();
        a.setId(1L);
        a.setClientId("c1");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(any())).thenReturn(a);

        Appointment result = service.cancel(1L, "c1");

        assertEquals(Appointment.Status.CANCELLED, result.getStatus());
    }

    @Test
    void cancel_wrongClient() {
        Appointment a = new Appointment();
        a.setId(1L);
        a.setClientId("owner");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));

        assertThrows(IllegalArgumentException.class,
                () -> service.cancel(1L, "other"));
    }

    // ---------- CONFIRM ----------
    @Test
    void confirm_success() {
        Appointment a = new Appointment();
        a.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));
        when(appointmentRepository.save(any())).thenReturn(a);

        Appointment result = service.confirm(1L);

        assertEquals(Appointment.Status.CONFIRMED, result.getStatus());
    }
}
