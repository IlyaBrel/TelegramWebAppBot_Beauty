package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.MasterDto;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.service.master.MasterService;
import ibrel.tgBeautyWebApp.service.master.MasterReviewService;
import ibrel.tgBeautyWebApp.web.mapper.MasterMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final MasterService masterService;
    private final MasterMapper masterMapper;
    private final MasterReviewService reviewService;

    @PostMapping
    public ResponseEntity<MasterDto> create(@Valid @RequestBody MasterDto dto) {
        Master entity = masterMapper.toEntity(dto);
        Master created = masterService.create(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(masterMapper.toDto(created, reviewService.getAverageRating(created.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MasterDto> update(@PathVariable Long id, @Valid @RequestBody MasterDto dto) {
        Master updated = masterService.update(id, masterMapper.toEntity(dto));
        return ResponseEntity.ok(masterMapper.toDto(updated, reviewService.getAverageRating(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MasterDto> getById(@PathVariable Long id) {
        Master m = masterService.getById(id);
        MasterDto dto = masterMapper.toDto(m, reviewService.getAverageRating(id));
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<MasterDto>> getAll() {
        List<MasterDto> list = masterService.getAll().stream()
                .map(m -> masterMapper.toDto(m, reviewService.getAverageRating(m.getId())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        masterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<MasterDto> activate(@PathVariable Long id) {
        Master m = masterService.activate(id);
        return ResponseEntity.ok(masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<MasterDto> deactivate(@PathVariable Long id) {
        Master m = masterService.deactivate(id);
        return ResponseEntity.ok(masterMapper.toDto(m, reviewService.getAverageRating(id)));
    }
}
