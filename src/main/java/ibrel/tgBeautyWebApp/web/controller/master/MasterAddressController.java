package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.MasterAddressDto;
import ibrel.tgBeautyWebApp.model.master.MasterAddress;
import ibrel.tgBeautyWebApp.service.master.MasterAddressService;
import ibrel.tgBeautyWebApp.web.mapper.MasterAddressMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/masters/{masterId}/address")
@RequiredArgsConstructor
public class MasterAddressController {

    private final MasterAddressService addressService;
    private final MasterAddressMapper mapper;

    @PostMapping
    public ResponseEntity<MasterAddressDto> create(@PathVariable Long masterId, @Valid @RequestBody MasterAddressDto dto) {
        MasterAddress created = addressService.create(masterId, mapper.toEntity(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PutMapping
    public ResponseEntity<MasterAddressDto> update(@PathVariable Long masterId, @Valid @RequestBody MasterAddressDto dto) {
        MasterAddress updated = addressService.update(masterId, mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @GetMapping
    public ResponseEntity<MasterAddressDto> get(@PathVariable Long masterId) {
        MasterAddress a = addressService.getByMasterId(masterId);
        return ResponseEntity.ok(mapper.toDto(a));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long masterId) {
        addressService.deleteByMasterId(masterId);
        return ResponseEntity.noContent().build();
    }
}
