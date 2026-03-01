package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import ibrel.tgBeautyWebApp.repository.MasterAddressRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.service.master.MasterAddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterAddressServiceImpl implements MasterAddressService {

    private final MasterRepository masterRepository;
    private final MasterAddressRepository addressRepository;

    @Override
    @Transactional
    public MasterAddress create(Long masterId, MasterAddress address) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(address, "address must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        if (master.getAddress() != null) {
            log.warn("Attempt to create address for master id={} which already has address id={}", masterId, master.getAddress().getId());
            throw new IllegalStateException("Address already exists for master");
        }

        validateAddressFields(address);

        address.setMaster(master);
        MasterAddress saved = addressRepository.save(address);
        master.setAddress(saved);
        masterRepository.save(master);

        log.info("Created address id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    @Transactional
    public MasterAddress update(Long masterId, MasterAddress address) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(address, "address must not be null");

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        MasterAddress existing = master.getAddress();
        if (existing == null) {
            address.setMaster(master);
            validateAddressFields(address);
            MasterAddress saved = addressRepository.save(address);
            master.setAddress(saved);
            masterRepository.save(master);
            log.info("Added new address id={} for master id={}", saved.getId(), masterId);
            return saved;
        }

        if (address.getCity() != null) existing.setCity(address.getCity());
        if (address.getStreet() != null) existing.setStreet(address.getStreet());
        if (address.getHouse() != null) existing.setHouse(address.getHouse());
        if (address.getFloor() != null) existing.setFloor(address.getFloor());
        if (address.getApartment() != null) existing.setApartment(address.getApartment());
        if (address.getPlaceOnTheMap() != null) existing.setPlaceOnTheMap(address.getPlaceOnTheMap());

        validateAddressFields(existing);

        MasterAddress saved = addressRepository.save(existing);
        log.info("Updated address id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    public MasterAddress getByMasterId(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        return addressRepository.findByMasterId(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found for master id=" + masterId));
    }

    @Override
    @Transactional
    public void deleteByMasterId(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        MasterAddress address = master.getAddress();
        if (address == null) {
            log.debug("No address to delete for master id={}", masterId);
            return;
        }

        master.setAddress(null);
        masterRepository.save(master);
        addressRepository.deleteById(address.getId());
        log.info("Deleted address id={} for master id={}", address.getId(), masterId);
    }

    private void validateAddressFields(MasterAddress address) {
        if (address.getCity() != null && address.getCity().length() > 100) {
            throw new IllegalArgumentException("City length must be <= 100");
        }
        if (address.getStreet() != null && address.getStreet().length() > 150) {
            throw new IllegalArgumentException("Street length must be <= 150");
        }
        if (address.getHouse() != null && address.getHouse().length() > 50) {
            throw new IllegalArgumentException("House length must be <= 50");
        }
        if (address.getFloor() != null && address.getFloor().length() > 20) {
            throw new IllegalArgumentException("Floor length must be <= 20");
        }
        if (address.getApartment() != null && address.getApartment().length() > 20) {
            throw new IllegalArgumentException("Apartment length must be <= 20");
        }
        if (address.getPlaceOnTheMap() != null && address.getPlaceOnTheMap().length() > 500) {
            throw new IllegalArgumentException("PlaceOnTheMap length must be <= 500");
        }
    }
}
