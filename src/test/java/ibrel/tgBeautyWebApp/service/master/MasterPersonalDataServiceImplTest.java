package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import ibrel.tgBeautyWebApp.repository.MasterPersonalDataRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.service.master.impl.MasterPersonalDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterPersonalDataServiceImplTest {

    @Mock MasterRepository masterRepository;
    @Mock MasterPersonalDataRepository personalDataRepository;

    @InjectMocks MasterPersonalDataServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void update_whenMasterExists_updatesData() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterPersonalData existing = new MasterPersonalData();
        existing.setId(10L);
        existing.setMaster(master);
        existing.setFirstName("Old");
        existing.setLastName("Name");

        // ВАЖНО: привязываем existing к master
        master.setPersonalData(existing);

        MasterPersonalData update = new MasterPersonalData();
        update.setFirstName("New");
        update.setLastName("Updated");
        update.setDescription("Desc");
        update.setPhone("12345");
        update.setExperienceYears(5);
        update.setCompletedJobs(100);
        update.setInstUserId("insta");

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(personalDataRepository.save(any())).thenReturn(existing);

        MasterPersonalData result = service.update(masterId, update);

        assertEquals("New", result.getFirstName());
        assertEquals("Updated", result.getLastName());
        assertEquals("Desc", result.getDescription());
        assertEquals("12345", result.getPhone());
        assertEquals(5, result.getExperienceYears());
        assertEquals(100, result.getCompletedJobs());
        assertEquals("insta", result.getInstUserId());
    }


    @Test
    void update_whenMasterMissing_throwsNotFound() {
        when(masterRepository.findById(999L)).thenReturn(Optional.empty());

        MasterPersonalData update = new MasterPersonalData();

        assertThrows(EntityNotFoundException.class,
                () -> service.update(999L, update));
    }

    @Test
    void update_whenPersonalDataMissing_createsNew() {
        Long masterId = 2L;

        Master master = new Master();
        master.setId(masterId);

        MasterPersonalData update = new MasterPersonalData();
        update.setFirstName("John");
        update.setLastName("Doe");

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(personalDataRepository.findByMasterId(masterId)).thenReturn(Optional.empty());

        MasterPersonalData saved = new MasterPersonalData();
        saved.setId(20L);
        saved.setMaster(master);
        saved.setFirstName("John");
        saved.setLastName("Doe");

        when(personalDataRepository.save(any())).thenReturn(saved);

        MasterPersonalData result = service.update(masterId, update);

        assertEquals(20L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(personalDataRepository).save(any());
    }
}
