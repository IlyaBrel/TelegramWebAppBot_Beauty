package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import ibrel.tgBeautyWebApp.repository.MasterAddressRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.service.master.impl.MasterAddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterAddressServiceImplTest {

    @Mock MasterRepository masterRepository;
    @Mock MasterAddressRepository addressRepository;

    @InjectMocks MasterAddressServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- CREATE ----------
    @Test
    void create_success() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterAddress address = new MasterAddress();
        address.setCity("City");

        MasterAddress saved = new MasterAddress();
        saved.setId(10L);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(addressRepository.save(any())).thenReturn(saved);

        MasterAddress result = service.create(masterId, address);

        assertEquals(10L, result.getId());
        verify(addressRepository).save(any());
        verify(masterRepository).save(master);
    }

    @Test
    void create_whenAddressExists_throwsIllegalState() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);
        master.setAddress(new MasterAddress()); // уже есть адрес

        MasterAddress newAddress = new MasterAddress();

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));

        assertThrows(IllegalStateException.class,
                () -> service.create(masterId, newAddress));
    }

    @Test
    void create_whenMasterMissing_throwsNotFound() {
        when(masterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.create(999L, new MasterAddress()));
    }

    // ---------- UPDATE ----------
    @Test
    void update_whenAddressExists_updatesFields() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterAddress existing = new MasterAddress();
        existing.setId(10L);
        existing.setCity("OldCity");
        master.setAddress(existing);

        MasterAddress update = new MasterAddress();
        update.setCity("NewCity");

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(addressRepository.save(any())).thenReturn(existing);

        MasterAddress result = service.update(masterId, update);

        assertEquals("NewCity", result.getCity());
        verify(addressRepository).save(existing);
    }

    @Test
    void update_whenAddressMissing_createsNew() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterAddress update = new MasterAddress();
        update.setCity("City");

        MasterAddress saved = new MasterAddress();
        saved.setId(20L);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(addressRepository.save(any())).thenReturn(saved);

        MasterAddress result = service.update(masterId, update);

        assertEquals(20L, result.getId());
        verify(addressRepository).save(any());
        verify(masterRepository).save(master);
    }

    // ---------- GET ----------
    @Test
    void getByMasterId_success() {
        MasterAddress addr = new MasterAddress();
        addr.setId(5L);

        when(addressRepository.findByMasterId(1L)).thenReturn(Optional.of(addr));

        MasterAddress result = service.getByMasterId(1L);

        assertEquals(5L, result.getId());
    }

    @Test
    void getByMasterId_notFound() {
        when(addressRepository.findByMasterId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getByMasterId(1L));
    }

    // ---------- DELETE ----------
    @Test
    void delete_whenAddressExists_deletes() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterAddress addr = new MasterAddress();
        addr.setId(10L);
        master.setAddress(addr);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));

        service.deleteByMasterId(masterId);

        verify(addressRepository).deleteById(10L);
        verify(masterRepository).save(master);
        assertNull(master.getAddress());
    }

    @Test
    void delete_whenAddressMissing_doesNothing() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);
        master.setAddress(null);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));

        service.deleteByMasterId(masterId);

        verify(addressRepository, never()).deleteById(any());
    }

    @Test
    void delete_whenMasterMissing_throwsNotFound() {
        when(masterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteByMasterId(999L));
    }
}
