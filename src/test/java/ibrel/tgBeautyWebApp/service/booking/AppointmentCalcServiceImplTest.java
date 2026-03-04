package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentCalcRequestDto;
import ibrel.tgBeautyWebApp.dto.booking.NearestSlotsRequestDto;
import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.enums.MasterServiceType;
import ibrel.tgBeautyWebApp.repository.*;
import ibrel.tgBeautyWebApp.service.booking.impl.AppointmentCalcServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentCalcServiceImplTest {

    @Mock MasterRepository masterRepository;
    @Mock WorkSlotRepository workSlotRepository;
    @Mock MasterServiceWorkRepository masterServiceWorkRepository;
    @Mock VariableServiceDetailsRepository variableServiceDetailsRepository;
    @Mock AppointmentRepository appointmentRepository;

    @InjectMocks AppointmentCalcServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- calculate() для FIXED ----------
    @Test
    void calculate_fixedService_success() {
        Long masterId = 1L;
        Long slotId = 10L;

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);

        ibrel.tgBeautyWebApp.model.master.Master master = new ibrel.tgBeautyWebApp.model.master.Master();
        master.setId(masterId);

        MasterServiceWork svc = new MasterServiceWork();
        svc.setId(100L);
        svc.setMaster(master);
        svc.setType(MasterServiceType.FIXED);

        FixedServiceDetails fixed = new FixedServiceDetails();
        fixed.setDurationMinutes(30);
        fixed.setPrice(120.0);
        fixed.setService(svc);
        svc.setFixedDetails(fixed);

        slot.setMaster(master);
        slot.setStartTime(LocalTime.of(9,0));
        slot.setEndTime(LocalTime.of(10,0));

        ServiceSelectionDto sel = new ServiceSelectionDto();
        sel.setServiceId(100L);

        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(svc));

        AppointmentCalcRequestDto req = AppointmentCalcRequestDto.builder()
                .masterId(masterId).slotId(slotId).services(List.of(sel)).build();

        var resp = service.calculate(req);

        assertEquals(30, resp.getTotalDurationMinutes());
        assertEquals(120.0, resp.getTotalPrice());
        assertTrue(Boolean.TRUE.equals(resp.getFitsInSlot()));
    }

    // ---------- calculate() для VARIABLE ----------
    @Test
    void calculate_variableService_success() {
        Long masterId = 1L;
        Long slotId = 10L;

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);

        ibrel.tgBeautyWebApp.model.master.Master master = new ibrel.tgBeautyWebApp.model.master.Master();
        master.setId(masterId);

        slot.setMaster(master);
        slot.setStartTime(LocalTime.of(8,0));
        slot.setEndTime(LocalTime.of(10,0));

        MasterServiceWork svc = new MasterServiceWork();
        svc.setId(200L);
        svc.setMaster(master);
        svc.setType(MasterServiceType.VARIABLE);

        VariableServiceDetails v = new VariableServiceDetails();
        v.setId(300L);
        v.setService(svc);
        v.setDurationMinutes(45);
        v.setPrice(60.0);

        ServiceSelectionDto sel = new ServiceSelectionDto();
        sel.setServiceId(200L);
        sel.setVariableDetailIds(List.of(300L));

        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(masterServiceWorkRepository.findById(200L)).thenReturn(Optional.of(svc));
        when(variableServiceDetailsRepository.findById(300L)).thenReturn(Optional.of(v));

        AppointmentCalcRequestDto req = AppointmentCalcRequestDto.builder()
                .masterId(masterId).slotId(slotId).services(List.of(sel)).build();

        var resp = service.calculate(req);

        assertEquals(45, resp.getTotalDurationMinutes());
        assertEquals(60.0, resp.getTotalPrice());
        assertTrue(Boolean.TRUE.equals(resp.getFitsInSlot()));
    }

    // ---------- calculate() ошибки: слот не найден ----------
    @Test
    void calculate_slotNotFound_throws() {
        when(workSlotRepository.findById(10L)).thenReturn(Optional.empty());

        AppointmentCalcRequestDto req = AppointmentCalcRequestDto.builder()
                .masterId(1L).slotId(10L).services(List.of(new ServiceSelectionDto())).build();

        assertThrows(EntityNotFoundException.class, () -> service.calculate(req));
    }

    // ---------- calculate() ошибки: услуга не найдена ----------
    @Test
    void calculate_serviceNotFound_throws() {
        Long masterId = 1L;
        Long slotId = 10L;

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);
        ibrel.tgBeautyWebApp.model.master.Master master = new ibrel.tgBeautyWebApp.model.master.Master();
        master.setId(masterId);
        slot.setMaster(master);
        slot.setStartTime(LocalTime.of(9,0));
        slot.setEndTime(LocalTime.of(10,0));

        ServiceSelectionDto sel = new ServiceSelectionDto();
        sel.setServiceId(999L);

        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(masterServiceWorkRepository.findById(999L)).thenReturn(Optional.empty());

        AppointmentCalcRequestDto req = AppointmentCalcRequestDto.builder()
                .masterId(masterId).slotId(slotId).services(List.of(sel)).build();

        assertThrows(EntityNotFoundException.class, () -> service.calculate(req));
    }

    // ---------- checkAvailability: мастер/слот/занятость/вместимость ----------
    @Test
    void checkAvailability_slotDoesNotBelong_returnsNotAvailable() {
        Long masterId = 1L;
        Long slotId = 10L;

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);
        ibrel.tgBeautyWebApp.model.master.Master otherMaster = new ibrel.tgBeautyWebApp.model.master.Master();
        otherMaster.setId(2L);
        slot.setMaster(otherMaster);

        when(masterRepository.existsById(masterId)).thenReturn(true);
        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        AppointmentCalcRequestDto req = AppointmentCalcRequestDto.builder()
                .masterId(masterId).slotId(slotId).services(List.of(new ServiceSelectionDto())).build();

        var resp = service.checkAvailability(req);

        assertFalse(Boolean.TRUE.equals(resp.getAvailable()));
        assertFalse(Boolean.TRUE.equals(resp.getFitsInSlot()));
        assertFalse(Boolean.TRUE.equals(resp.getSlotFree()));
        assertEquals("Slot does not belong to master", resp.getReason());
    }

    @Test
    void checkAvailability_slotBooked_returnsNotAvailable() {
        Long masterId = 1L;
        Long slotId = 10L;

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);
        ibrel.tgBeautyWebApp.model.master.Master master = new ibrel.tgBeautyWebApp.model.master.Master();
        master.setId(masterId);
        slot.setMaster(master);
        slot.setStartTime(LocalTime.of(9,0));
        slot.setEndTime(LocalTime.of(10,0));

        ServiceSelectionDto sel = new ServiceSelectionDto();
        sel.setServiceId(100L);

        MasterServiceWork svc = new MasterServiceWork();
        svc.setId(100L);
        svc.setMaster(master);
        svc.setType(MasterServiceType.FIXED);

        FixedServiceDetails fixed = new FixedServiceDetails();
        fixed.setDurationMinutes(30);
        fixed.setPrice(10.0);
        fixed.setService(svc);
        svc.setFixedDetails(fixed);

        when(masterRepository.existsById(masterId)).thenReturn(true);
        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlot_Id(slotId)).thenReturn(true);
        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(svc));

        AppointmentCalcRequestDto req = AppointmentCalcRequestDto.builder()
                .masterId(masterId).slotId(slotId).services(List.of(sel)).build();

        var resp = service.checkAvailability(req);

        assertFalse(Boolean.TRUE.equals(resp.getAvailable()));
        assertFalse(Boolean.TRUE.equals(resp.getSlotFree()));
        assertTrue(Boolean.TRUE.equals(resp.getFitsInSlot())); // fits but slot is booked
        assertEquals("Slot already booked", resp.getReason());
    }

    @Test
    void checkAvailability_durationExceeds_returnsNotAvailable() {
        Long masterId = 1L;
        Long slotId = 10L;

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);
        ibrel.tgBeautyWebApp.model.master.Master master = new ibrel.tgBeautyWebApp.model.master.Master();
        master.setId(masterId);
        slot.setMaster(master);
        slot.setStartTime(LocalTime.of(9,0));
        slot.setEndTime(LocalTime.of(9,30)); // 30 minutes

        ServiceSelectionDto sel = new ServiceSelectionDto();
        sel.setServiceId(100L);

        MasterServiceWork svc = new MasterServiceWork();
        svc.setId(100L);
        svc.setMaster(master);
        svc.setType(MasterServiceType.FIXED);

        FixedServiceDetails fixed = new FixedServiceDetails();
        fixed.setDurationMinutes(60); // exceeds slot
        fixed.setPrice(10.0);
        fixed.setService(svc);
        svc.setFixedDetails(fixed);

        when(masterRepository.existsById(masterId)).thenReturn(true);
        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlot_Id(slotId)).thenReturn(false);
        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(svc));

        AppointmentCalcRequestDto req = AppointmentCalcRequestDto.builder()
                .masterId(masterId).slotId(slotId).services(List.of(sel)).build();

        var resp = service.checkAvailability(req);

        assertFalse(Boolean.TRUE.equals(resp.getAvailable()));
        assertFalse(Boolean.TRUE.equals(resp.getFitsInSlot()));
        assertEquals("Total duration exceeds slot length", resp.getReason());
    }

    // ---------- findNearestSlots and findOptimalSlot ----------
    @Test
    void findNearestSlots_filtersAndSorts_and_findOptimalSlot_returnsFirst() {
        Long masterId = 1L;

        // two slots: one today later, one tomorrow earlier
        WorkSlot slot1 = new WorkSlot();
        slot1.setId(11L);
        ibrel.tgBeautyWebApp.model.master.Master master = new ibrel.tgBeautyWebApp.model.master.Master();
        master.setId(masterId);
        slot1.setMaster(master);
        slot1.setDayOfWeek(LocalDate.now().getDayOfWeek().name()); // today
        slot1.setStartTime(LocalTime.of(18,0));
        slot1.setEndTime(LocalTime.of(19,0));

        WorkSlot slot2 = new WorkSlot();
        slot2.setId(12L);
        slot2.setMaster(master);
        slot2.setDayOfWeek(LocalDate.now().plusDays(1).getDayOfWeek().name()); // tomorrow
        slot2.setStartTime(LocalTime.of(9,0));
        slot2.setEndTime(LocalTime.of(10,0));

        when(masterRepository.existsById(masterId)).thenReturn(true);
        when(workSlotRepository.findByMasterId(masterId)).thenReturn(List.of(slot1, slot2));
        when(workSlotRepository.findById(11L)).thenReturn(Optional.of(slot1));
        when(workSlotRepository.findById(12L)).thenReturn(Optional.of(slot2));
        when(appointmentRepository.existsBySlot_Id(11L)).thenReturn(false);
        when(appointmentRepository.existsBySlot_Id(12L)).thenReturn(false);


        // service that fits into both slots (15 minutes)
        MasterServiceWork svc = new MasterServiceWork();
        svc.setId(100L);
        svc.setMaster(master);
        svc.setType(MasterServiceType.FIXED);

        FixedServiceDetails fixed = new FixedServiceDetails();
        fixed.setDurationMinutes(15);
        fixed.setPrice(10.0);
        fixed.setService(svc);
        svc.setFixedDetails(fixed);

        when(masterServiceWorkRepository.findById(100L)).thenReturn(Optional.of(svc));

        NearestSlotsRequestDto req = NearestSlotsRequestDto.builder()
                .masterId(masterId)
                .services(List.of(new ServiceSelectionDto(){{
                    setServiceId(100L);
                }}))
                .limit(5)
                .build();

        var nearest = service.findNearestSlots(req);
        assertFalse(nearest.isEmpty());
        var optimal = service.findOptimalSlot(req);
        assertTrue(Boolean.TRUE.equals(optimal.getFitsInSlot()));
        assertNotNull(optimal.getSlot());
    }
}
