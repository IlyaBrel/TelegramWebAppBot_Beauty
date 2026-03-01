package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.*;
import ibrel.tgBeautyWebApp.model.master.service.FixedServiceDetails;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import ibrel.tgBeautyWebApp.service.master.MasterServiceWorkService;
import ibrel.tgBeautyWebApp.web.mapper.MasterServiceWorkMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MasterServiceWorkController {

    private final MasterServiceWorkService service;
    private final MasterServiceWorkMapper mapper;

    @GetMapping("/masters/{masterId}/services")
    public ResponseEntity<List<MasterServiceWorkDto>> getByMaster(@PathVariable Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        List<MasterServiceWorkDto> list = service.getByMaster(masterId).stream().map(mapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/masters/{masterId}/services")
    public ResponseEntity<MasterServiceWorkDto> create(@PathVariable Long masterId, @Valid @RequestBody MasterServiceWorkDto dto) {
        MasterServiceWork entity = mapper.toEntity(dto);
        MasterServiceWork created = service.create(masterId, entity);
        return ResponseEntity.ok(mapper.toDto(created));
    }

    @PutMapping("/services/{serviceId}")
    public ResponseEntity<MasterServiceWorkDto> update(@PathVariable Long serviceId, @RequestBody MasterServiceWorkDto dto) {
        MasterServiceWork entity = mapper.toEntity(dto);
        MasterServiceWork updated = service.update(serviceId, entity);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/masters/{masterId}/services/{serviceId}")
    public ResponseEntity<Void> delete(@PathVariable Long masterId, @PathVariable Long serviceId) {
        service.delete(serviceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/services/{serviceId}/fixed")
    public ResponseEntity<FixedServiceDetailsDto> addOrUpdateFixed(@PathVariable Long serviceId, @RequestBody FixedServiceDetailsDto dto) {
        FixedServiceDetails fd = new FixedServiceDetails();
        fd.setId(dto.getId());
        fd.setDurationMinutes(dto.getDurationMinutes());
        fd.setPrice(dto.getPrice());
        fd.setDescription(dto.getDescription());
        FixedServiceDetails saved = service.addOrUpdateFixedDetails(serviceId, fd);
        FixedServiceDetailsDto out = FixedServiceDetailsDto.builder()
                .id(saved.getId())
                .durationMinutes(saved.getDurationMinutes())
                .price(saved.getPrice())
                .description(saved.getDescription())
                .build();
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/services/{serviceId}/fixed")
    public ResponseEntity<Void> removeFixed(@PathVariable Long serviceId) {
        service.removeFixedDetails(serviceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/services/{serviceId}/variable")
    public ResponseEntity<VariableServiceDetailsDto> addVariable(@PathVariable Long serviceId, @RequestBody VariableServiceDetailsDto dto) {
        VariableServiceDetails v = new VariableServiceDetails();
        v.setFactorName(dto.getFactorName());
        v.setFactorValue(dto.getFactorValue());
        v.setDurationMinutes(dto.getDurationMinutes());
        v.setPrice(dto.getPrice());
        v.setDescription(dto.getDescription());
        VariableServiceDetails saved = service.addVariableDetail(serviceId, v);
        VariableServiceDetailsDto out = VariableServiceDetailsDto.builder()
                .id(saved.getId())
                .factorName(saved.getFactorName())
                .factorValue(saved.getFactorValue())
                .durationMinutes(saved.getDurationMinutes())
                .price(saved.getPrice())
                .description(saved.getDescription())
                .build();
        return ResponseEntity.ok(out);
    }

    @PutMapping("/services/variable/{variableId}")
    public ResponseEntity<VariableServiceDetailsDto> updateVariable(@PathVariable Long variableId, @RequestBody VariableServiceDetailsDto dto) {
        VariableServiceDetails v = new VariableServiceDetails();
        v.setFactorName(dto.getFactorName());
        v.setFactorValue(dto.getFactorValue());
        v.setDurationMinutes(dto.getDurationMinutes());
        v.setPrice(dto.getPrice());
        v.setDescription(dto.getDescription());
        VariableServiceDetails updated = service.updateVariableDetail(variableId, v);
        VariableServiceDetailsDto out = VariableServiceDetailsDto.builder()
                .id(updated.getId())
                .factorName(updated.getFactorName())
                .factorValue(updated.getFactorValue())
                .durationMinutes(updated.getDurationMinutes())
                .price(updated.getPrice())
                .description(updated.getDescription())
                .build();
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/services/variable/{variableId}")
    public ResponseEntity<Void> deleteVariable(@PathVariable Long variableId) {
        service.removeVariableDetail(variableId);
        return ResponseEntity.noContent().build();
    }
}
