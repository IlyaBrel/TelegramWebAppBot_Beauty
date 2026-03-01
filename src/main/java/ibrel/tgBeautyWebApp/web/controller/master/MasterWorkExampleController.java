package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.MasterWorkExampleDto;
import ibrel.tgBeautyWebApp.model.master.MasterWorkExample;
import ibrel.tgBeautyWebApp.service.master.MasterWorkExampleService;
import ibrel.tgBeautyWebApp.web.mapper.MasterWorkExampleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MasterWorkExampleController {

    private final MasterWorkExampleService exampleService;
    private final MasterWorkExampleMapper mapper;

    @GetMapping("/masters/{masterId}/works")
    public ResponseEntity<List<MasterWorkExampleDto>> getByMaster(@PathVariable Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        List<MasterWorkExampleDto> list = exampleService.getByMaster(masterId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/masters/{masterId}/works")
    public ResponseEntity<MasterWorkExampleDto> addWork(@PathVariable Long masterId,
                                                        @Valid @RequestBody MasterWorkExampleDto dto) {
        MasterWorkExample entity = mapper.toEntity(dto);
        MasterWorkExample created = exampleService.add(masterId, entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @GetMapping("/works/{exampleId}")
    public ResponseEntity<MasterWorkExampleDto> getWork(@PathVariable Long exampleId) {
        MasterWorkExample e = exampleService.getById(exampleId);
        return ResponseEntity.ok(mapper.toDto(e));
    }

    @DeleteMapping("/works/{exampleId}")
    public ResponseEntity<Void> deleteWork(@PathVariable Long exampleId) {
        exampleService.delete(exampleId);
        return ResponseEntity.noContent().build();
    }
}
