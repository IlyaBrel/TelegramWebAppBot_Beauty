package ibrel.tgBeautyWebApp.web.controller.appointment;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentRequestDto;
import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import ibrel.tgBeautyWebApp.web.mapper.AppointmentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper mapper;

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(@Valid @RequestBody AppointmentRequestDto request) {
        var appt = appointmentService.create(mapper.toRequest(request));
        var dto = mapper.toDto(appt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getById(@PathVariable Long id) {
        var appt = appointmentService.getById(id);
        return ResponseEntity.ok(mapper.toDto(appt));
    }

    @GetMapping("/master/{masterId}")
    public ResponseEntity<List<AppointmentResponseDto>> getByMaster(@PathVariable Long masterId) {
        var list = appointmentService.getByMaster(masterId);
        var dtos = list.stream().map(mapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AppointmentResponseDto>> getByUser(@PathVariable Long userId) {
        var list = appointmentService.getByUser(userId);
        var dtos = list.stream().map(mapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable Long id,
                                                         @RequestParam(required = false) Long requestedByUserId) {
        var appt = appointmentService.cancel(id, requestedByUserId);
        return ResponseEntity.ok(mapper.toDto(appt));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDto> complete(@PathVariable Long id) {
        var appt = appointmentService.complete(id);
        return ResponseEntity.ok(mapper.toDto(appt));
    }
}
