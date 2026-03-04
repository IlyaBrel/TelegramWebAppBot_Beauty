package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.*;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.service.master.impl.MasterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterServiceImplTest {

    @Mock MasterRepository masterRepository;

    @Mock MasterAddressService addressService;
    @Mock MasterPersonalDataService personalDataService;
    @Mock MasterServiceWorkService serviceWorkService;
    @Mock WorkSlotService slotService;
    @Mock MasterReviewService reviewService;
    @Mock MasterWorkExampleService workExampleService;

    @InjectMocks MasterServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- CREATE ----------
    @Test
    void create_success() {
        Master master = new Master();
        master.setId(1L);

        when(masterRepository.save(any())).thenReturn(master);

        Master result = service.create(master);

        assertEquals(1L, result.getId());
        verify(masterRepository).save(master);
    }

    // ---------- UPDATE ----------
    @Test
    void update_success() {
        Long id = 1L;

        Master existing = new Master();
        existing.setId(id);
        existing.setActive(false);
        existing.setImageUrl("old");

        Master updated = new Master();
        updated.setActive(true);
        updated.setImageUrl("new");

        when(masterRepository.findById(id)).thenReturn(Optional.of(existing));
        when(masterRepository.save(any())).thenReturn(existing);

        Master result = service.update(id, updated);

        assertTrue(result.getActive());
        assertEquals("new", result.getImageUrl());
        verify(masterRepository).save(existing);
    }

    @Test
    void update_masterNotFound() {
        when(masterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.update(1L, new Master()));
    }

    // ---------- GET ----------
    @Test
    void getById_success() {
        Master master = new Master();
        master.setId(1L);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));

        Master result = service.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getById_notFound() {
        when(masterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getById(1L));
    }

    @Test
    void getAll_success() {
        List<Master> list = List.of(new Master(), new Master());
        when(masterRepository.findAll()).thenReturn(list);

        List<Master> result = service.getAll();

        assertEquals(2, result.size());
    }

    // ---------- DELETE ----------
    @Test
    void delete_success() {
        when(masterRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(masterRepository).deleteById(1L);
    }

    @Test
    void delete_notFound() {
        when(masterRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(1L));
    }

    // ---------- ACTIVATE / DEACTIVATE ----------
    @Test
    void activate_success() {
        Master master = new Master();
        master.setId(1L);
        master.setActive(false);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));

        Master result = service.activate(1L);

        assertTrue(result.getActive());
        verify(masterRepository).save(master);
    }

    @Test
    void deactivate_success() {
        Master master = new Master();
        master.setId(1L);
        master.setActive(true);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));

        Master result = service.deactivate(1L);

        assertFalse(result.getActive());
        verify(masterRepository).save(master);
    }

    // ---------- DELEGATION TESTS ----------
    @Test
    void updateAddress_delegates() {
        MasterAddress address = new MasterAddress();
        when(addressService.update(1L, address)).thenReturn(address);

        MasterAddress result = service.updateAddress(1L, address);

        assertEquals(address, result);
        verify(addressService).update(1L, address);
    }

    @Test
    void updatePersonalData_delegates() {
        MasterPersonalData data = new MasterPersonalData();
        when(personalDataService.update(1L, data)).thenReturn(data);

        MasterPersonalData result = service.updatePersonalData(1L, data);

        assertEquals(data, result);
        verify(personalDataService).update(1L, data);
    }

    @Test
    void addServiceToMaster_delegates() {
        MasterServiceWork work = new MasterServiceWork();
        when(serviceWorkService.create(1L, work)).thenReturn(work);

        MasterServiceWork result = service.addServiceToMaster(1L, work);

        assertEquals(work, result);
        verify(serviceWorkService).create(1L, work);
    }

    @Test
    void removeServiceFromMaster_delegates() {
        service.removeServiceFromMaster(1L, 2L);
        verify(serviceWorkService).delete(2L);
    }

    @Test
    void getServices_delegates() {
        List<MasterServiceWork> list = List.of(new MasterServiceWork());
        when(serviceWorkService.getByMaster(1L)).thenReturn(list);

        List<MasterServiceWork> result = service.getServices(1L);

        assertEquals(1, result.size());
    }

    @Test
    void addWorkSlot_delegates() {
        WorkSlot slot = new WorkSlot();
        when(slotService.create(1L, slot)).thenReturn(slot);

        WorkSlot result = service.addWorkSlot(1L, slot);

        assertEquals(slot, result);
        verify(slotService).create(1L, slot);
    }

    @Test
    void removeWorkSlot_delegates() {
        service.removeWorkSlot(1L, 2L);
        verify(slotService).delete(2L);
    }

    @Test
    void getSlots_delegates() {
        List<WorkSlot> list = List.of(new WorkSlot());
        when(slotService.findByMaster(1L)).thenReturn(list);

        List<WorkSlot> result = service.getSlots(1L);

        assertEquals(1, result.size());
    }

    @Test
    void addReview_delegates() {
        MasterReview review = new MasterReview();
        when(reviewService.addReview(1L, review)).thenReturn(review);

        MasterReview result = service.addReview(1L, review);

        assertEquals(review, result);
        verify(reviewService).addReview(1L, review);
    }

    @Test
    void getReviews_delegates() {
        List<MasterReview> list = List.of(new MasterReview());
        when(reviewService.getByMaster(1L)).thenReturn(list);

        List<MasterReview> result = service.getReviews(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAverageRating_delegates() {
        when(reviewService.getAverageRating(1L)).thenReturn(4.5);

        double result = service.getAverageRating(1L);

        assertEquals(4.5, result);
    }

    @Test
    void addWorkExample_delegates() {
        MasterWorkExample example = new MasterWorkExample();
        when(workExampleService.add(1L, example)).thenReturn(example);

        MasterWorkExample result = service.addWorkExample(1L, example);

        assertEquals(example, result);
        verify(workExampleService).add(1L, example);
    }

    @Test
    void getWorks_delegates() {
        List<MasterWorkExample> list = List.of(new MasterWorkExample());
        when(workExampleService.getByMaster(1L)).thenReturn(list);

        List<MasterWorkExample> result = service.getWorks(1L);

        assertEquals(1, result.size());
    }

    // ---------- GET APPOINTMENTS ----------
    @Test
    void getAppointments_success() {
        Master master = new Master();
        master.setId(1L);
        master.setAppointments(List.of(new Appointment()));

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));

        List<Appointment> result = service.getAppointments(1L);

        assertEquals(1, result.size());
    }
}
