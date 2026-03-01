package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.MasterPersonalDataDto;
import ibrel.tgBeautyWebApp.model.master.MasterPersonalData;
import ibrel.tgBeautyWebApp.service.master.MasterPersonalDataService;
import ibrel.tgBeautyWebApp.web.mapper.MasterPersonalDataMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/masters/{masterId}/personal")
@RequiredArgsConstructor
public class MasterPersonalDataController {

    private final MasterPersonalDataService personalDataService;
    private final MasterPersonalDataMapper mapper;

    @PostMapping
    public ResponseEntity<MasterPersonalDataDto> create(@PathVariable Long masterId,
                                                        @Valid @RequestBody MasterPersonalDataDto dto) {
        MasterPersonalData created = personalDataService.create(masterId, mapper.toEntity(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PutMapping
    public ResponseEntity<MasterPersonalDataDto> update(@PathVariable Long masterId,
                                                        @Valid @RequestBody MasterPersonalDataDto dto) {
        MasterPersonalData updated = personalDataService.update(masterId, mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @GetMapping
    public ResponseEntity<MasterPersonalDataDto> get(@PathVariable Long masterId) {
        MasterPersonalData pd = personalDataService.getByMasterId(masterId);
        return ResponseEntity.ok(mapper.toDto(pd));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long masterId) {
        personalDataService.deleteByMasterId(masterId);
        return ResponseEntity.noContent().build();
    }
}
