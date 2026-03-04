package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterWorkExample;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.MasterWorkExampleRepository;
import ibrel.tgBeautyWebApp.service.master.impl.MasterWorkExampleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterWorkExampleServiceImplTest {

    @Mock MasterRepository masterRepository;
    @Mock MasterWorkExampleRepository exampleRepository;

    @InjectMocks MasterWorkExampleServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- ADD ----------
    @Test
    void add_success() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterWorkExample example = new MasterWorkExample();
        example.setTitle("Work 1");

        MasterWorkExample saved = new MasterWorkExample();
        saved.setId(10L);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(exampleRepository.save(any())).thenReturn(saved);

        MasterWorkExample result = service.add(masterId, example);

        assertEquals(10L, result.getId());
        verify(exampleRepository).save(any());
    }

    @Test
    void add_whenMasterMissing_throwsNotFound() {
        when(masterRepository.findById(999L)).thenReturn(Optional.empty());

        MasterWorkExample example = new MasterWorkExample();
        example.setTitle("Work");

        assertThrows(EntityNotFoundException.class,
                () -> service.add(999L, example));
    }

    @Test
    void add_whenTitleMissing_throwsIllegalArgument() {
        Long masterId = 1L;

        MasterWorkExample example = new MasterWorkExample();
        example.setTitle(" "); // blank

        assertThrows(IllegalArgumentException.class,
                () -> service.add(masterId, example));
    }

    // ---------- GET BY ID ----------
    @Test
    void getById_success() {
        MasterWorkExample example = new MasterWorkExample();
        example.setId(5L);

        when(exampleRepository.findById(5L)).thenReturn(Optional.of(example));

        MasterWorkExample result = service.getById(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void getById_notFound() {
        when(exampleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getById(5L));
    }

    // ---------- GET BY MASTER ----------
    @Test
    void getByMaster_success() {
        Long masterId = 1L;

        when(masterRepository.existsById(masterId)).thenReturn(true);

        List<MasterWorkExample> list = List.of(new MasterWorkExample());
        when(exampleRepository.findByMasterIdOrderByCreatedAtDesc(masterId)).thenReturn(list);

        List<MasterWorkExample> result = service.getByMaster(masterId);

        assertEquals(1, result.size());
    }

    @Test
    void getByMaster_masterNotFound() {
        when(masterRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.getByMaster(1L));
    }
}
