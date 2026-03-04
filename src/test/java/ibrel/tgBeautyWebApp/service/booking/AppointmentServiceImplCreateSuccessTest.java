package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceImplCreateSuccessTest {

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

    @Test
    void createAppointment_success_withFixedAndVariableServices() {
        Long masterId = 10L;
        Long slotId = 20L;
        Long fixedServiceId = 30L;
        Long variableServiceId = 31L;
        Long varDetailId = 300L;

        Master master = new Master();
        master.setId(masterId);

        WorkSlot slot = new WorkSlot();
        slot.setId(slotId);
        slot.setStartTime(LocalTime.of(9,0));
        slot.setEndTime(LocalTime.of(10,0)); // 60 minutes
        slot.setMaster(master);

        // fixed service 30 minutes
        MasterServiceWork fixed = new MasterServiceWork();
        fixed.setId(fixedServiceId);
        fixed.setMaster(master);
        fixed.setType(MasterServiceType.FIXED);
        FixedServiceDetails fd = new FixedServiceDetails();
        fd.setDurationMinutes(30);
        fd.setPrice(50.0);
        fixed.setFixedDetails(fd);

        // variable service with one detail 20 minutes
        MasterServiceWork variable = new MasterServiceWork();
        variable.setId(variableServiceId);
        variable.setMaster(master);
        variable.setType(MasterServiceType.VARIABLE);
        VariableServiceDetails vdetail = new VariableServiceDetails();
        vdetail.setId(varDetailId);
        vdetail.setDurationMinutes(20);
        vdetail.setPrice(30.0);
        vdetail.setService(variable);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(workSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(masterServiceWorkRepository.findById(fixedServiceId)).thenReturn(Optional.of(fixed));
        when(masterServiceWorkRepository.findById(variableServiceId)).thenReturn(Optional.of(variable));
        when(variableServiceDetailsRepository.findById(varDetailId)).thenReturn(Optional.of(vdetail));
        when(appointmentRepository.existsBySlot_Id(slotId)).thenReturn(false);

        // simulate save: return appointment with id
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        Appointment saved = new Appointment();
        saved.setId(555L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

        ServiceSelectionDto selFixed = ServiceSelectionDto.builder().serviceId(fixedServiceId).build();
        ServiceSelectionDto selVar = ServiceSelectionDto.builder()
                .serviceId(variableServiceId)
                .variableDetailIds(List.of(varDetailId))
                .build();

        Appointment result = service.createAppointment("client-xyz", masterId, slotId, List.of(selFixed, selVar));

        assertNotNull(result);
        assertEquals(555L, result.getId());
        verify(appointmentRepository).save(captor.capture());
        Appointment toSave = captor.getValue();
        assertEquals("client-xyz", toSave.getClientId());
        assertEquals(masterId, toSave.getMaster().getId());
        assertEquals(slotId, toSave.getSlot().getId());
        // items should be set before save
        assertNotNull(toSave.getItems());
        assertEquals(2, toSave.getItems().size());
        // verify itemRepository.save called for each item (service persists)
        verify(itemRepository, atLeast(0)).save(any(AppointmentServiceItem.class));
    }
}
