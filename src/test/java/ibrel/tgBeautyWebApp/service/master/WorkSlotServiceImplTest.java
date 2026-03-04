package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.WorkSlotRepository;
import ibrel.tgBeautyWebApp.service.master.impl.WorkSlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkSlotServiceImplTest {

    @Mock MasterRepository masterRepository;
    @Mock WorkSlotRepository workSlotRepository;
    @Mock AppointmentRepository appointmentRepository;

    @InjectMocks WorkSlotServiceImpl workSlotService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_whenOverlapping_throwsIllegalArgument() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        // существующий слот 10:00–11:00
        WorkSlot existing = new WorkSlot();
        existing.setId(100L);
        existing.setMaster(master);
        existing.setDayOfWeek(String.valueOf(DayOfWeek.MONDAY));
        existing.setStartTime(LocalTime.of(10, 0));
        existing.setEndTime(LocalTime.of(11, 0));

        // новый слот 10:30–11:30 (пересекается)
        WorkSlot newSlot = new WorkSlot();
        newSlot.setDayOfWeek(String.valueOf(DayOfWeek.MONDAY));
        newSlot.setStartTime(LocalTime.of(10, 30));
        newSlot.setEndTime(LocalTime.of(11, 30));

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(workSlotRepository.findByMasterIdAndDayOfWeek(masterId, String.valueOf(DayOfWeek.MONDAY)))
                .thenReturn(List.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> workSlotService.create(masterId, newSlot)
        );

        assertTrue(ex.getMessage().contains("overlaps"));
        verify(workSlotRepository, never()).save(any());
    }

    @Test
    void create_whenNoOverlap_savesSlot() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        // существующий слот 08:00–09:00
        WorkSlot existing = new WorkSlot();
        existing.setId(101L);
        existing.setMaster(master);
        existing.setDayOfWeek(String.valueOf(DayOfWeek.MONDAY));
        existing.setStartTime(LocalTime.of(8, 0));
        existing.setEndTime(LocalTime.of(9, 0));

        // новый слот 09:30–10:30 (не пересекается)
        WorkSlot newSlot = new WorkSlot();
        newSlot.setDayOfWeek(String.valueOf(DayOfWeek.MONDAY));
        newSlot.setStartTime(LocalTime.of(9, 30));
        newSlot.setEndTime(LocalTime.of(10, 30));

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(workSlotRepository.findByMasterIdAndDayOfWeek(masterId, String.valueOf(DayOfWeek.MONDAY)))
                .thenReturn(List.of(existing));

        WorkSlot saved = new WorkSlot();
        saved.setId(777L);

        when(workSlotRepository.save(any())).thenReturn(saved);

        WorkSlot result = workSlotService.create(masterId, newSlot);

        assertEquals(777L, result.getId());
        verify(workSlotRepository).save(any());
    }
}
