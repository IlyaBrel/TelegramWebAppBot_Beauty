package ibrel.tgBeautyWebApp.service.master.impl;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterWorkExample;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.MasterWorkExampleRepository;
import ibrel.tgBeautyWebApp.service.master.MasterWorkExampleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterWorkExampleServiceImpl implements MasterWorkExampleService {

    private final MasterRepository masterRepository;
    private final MasterWorkExampleRepository exampleRepository;

    @Override
    @Transactional
    public MasterWorkExample add(Long masterId, MasterWorkExample example) {
        Assert.notNull(masterId, "masterId must not be null");
        Assert.notNull(example, "example must not be null");
        if (example.getTitle() == null || example.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new EntityNotFoundException("Master not found id=" + masterId));

        example.setMaster(master);
        example.setCreatedAt(OffsetDateTime.now());
        MasterWorkExample saved = exampleRepository.save(example);
        log.info("Added work example id={} for master id={}", saved.getId(), masterId);
        return saved;
    }

    @Override
    public MasterWorkExample getById(Long id) {
        Assert.notNull(id, "id must not be null");
        return exampleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work example not found id=" + id));
    }

    @Override
    public List<MasterWorkExample> getByMaster(Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        if (!masterRepository.existsById(masterId)) throw new EntityNotFoundException("Master not found id=" + masterId);
        return exampleRepository.findByMasterIdOrderByCreatedAtDesc(masterId);
    }

    @Override
    @Transactional
    public void delete(Long exampleId) {
        Assert.notNull(exampleId, "exampleId must not be null");
        MasterWorkExample existing = exampleRepository.findById(exampleId)
                .orElseThrow(() -> new EntityNotFoundException("Work example not found id=" + exampleId));

        // Если в будущем появится связь с Appointment или заказами — здесь добавить проверку использования.
        exampleRepository.deleteById(exampleId);
        log.info("Deleted work example id={}", exampleId);
    }
}
