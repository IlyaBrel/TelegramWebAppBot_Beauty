package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.enums.MasterServiceType;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.MasterServiceWorkRepository;
import ibrel.tgBeautyWebApp.repository.VariableServiceDetailsRepository;
import ibrel.tgBeautyWebApp.service.master.impl.MasterServiceWorkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterServiceWorkServiceImplTest {

    @Mock MasterRepository masterRepository;
    @Mock MasterServiceWorkRepository masterServiceWorkRepository;
    @Mock VariableServiceDetailsRepository variableServiceDetailsRepository;
    @Mock AppointmentRepository appointmentRepository;

    @InjectMocks MasterServiceWorkServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_whenServiceUsed_throwsIllegalState() {
        Long serviceId = 10L;

        MasterServiceWork svc = new MasterServiceWork();
        svc.setId(serviceId);

        when(masterServiceWorkRepository.findById(serviceId))
                .thenReturn(Optional.of(svc));

        when(appointmentRepository.existsByItems_Service_Id(serviceId))
                .thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.delete(serviceId)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("cannot") ||
                ex.getMessage().toLowerCase().contains("used"));

        verify(masterServiceWorkRepository, never()).deleteById(serviceId);
    }

    @Test
    void delete_whenServiceNotUsed_deletesSuccessfully() {
        Long serviceId = 11L;

        MasterServiceWork svc = new MasterServiceWork();
        svc.setId(serviceId);

        when(masterServiceWorkRepository.findById(serviceId))
                .thenReturn(Optional.of(svc));

        when(appointmentRepository.existsByItems_Service_Id(serviceId))
                .thenReturn(false);

        service.delete(serviceId);

        verify(masterServiceWorkRepository).deleteById(serviceId);
    }

    @Test
    void createFixedService_success() {
        Long masterId = 5L;

        Master master = new Master();
        master.setId(masterId);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));

        MasterServiceWork input = new MasterServiceWork();
        input.setName("Test Fixed Service"); // ← обязательное поле
        input.setType(MasterServiceType.FIXED);

        FixedServiceDetails fd = new FixedServiceDetails();
        fd.setDurationMinutes(30);
        fd.setPrice(50.0);
        input.setFixedDetails(fd);

        MasterServiceWork saved = new MasterServiceWork();
        saved.setId(100L);

        when(masterServiceWorkRepository.save(any())).thenReturn(saved);

        MasterServiceWork result = service.create(masterId, input);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(masterServiceWorkRepository).save(any());
    }


    @Test
    void createVariableService_withDetails_success() {
        Long masterId = 6L;

        Master master = new Master();
        master.setId(masterId);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));

        MasterServiceWork input = new MasterServiceWork();
        input.setName("Variable Test Service"); // ← обязательное поле
        input.setType(MasterServiceType.VARIABLE);

        VariableServiceDetails d1 = new VariableServiceDetails();
        d1.setFactorName("Length");
        d1.setFactorValue("Short");
        d1.setDurationMinutes(20);
        d1.setPrice(30.0);

        VariableServiceDetails d2 = new VariableServiceDetails();
        d2.setFactorName("Length");
        d2.setFactorValue("Long");
        d2.setDurationMinutes(40);
        d2.setPrice(60.0);

        input.setVariableDetails(List.of(d1, d2));

        MasterServiceWork saved = new MasterServiceWork();
        saved.setId(200L);

        when(masterServiceWorkRepository.save(any())).thenReturn(saved);

        MasterServiceWork result = service.create(masterId, input);

        assertEquals(200L, result.getId());
        verify(masterServiceWorkRepository).save(any());
    }


    @Test
    void create_whenMasterMissing_throwsEntityNotFound() {
        Long masterId = 999L;

        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        MasterServiceWork input = new MasterServiceWork();
        input.setType(MasterServiceType.FIXED);

        assertThrows(EntityNotFoundException.class,
                () -> service.create(masterId, input));
    }
}
