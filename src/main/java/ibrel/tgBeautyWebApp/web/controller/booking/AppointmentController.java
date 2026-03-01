package ibrel.tgBeautyWebApp.web.controller.booking;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentCreateRequestDto;
import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.dto.booking.ServiceSelectionDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import ibrel.tgBeautyWebApp.web.mapper.AppointmentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper mapper;

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponseDto> create(@Valid @RequestBody AppointmentCreateRequestDto req) {
        Appointment created = appointmentService.createAppointment(
                req.getClientId(),
                req.getMasterId(),
                req.getSlotId(),
                req.getServices()
        );

        // пересчёт totalDuration/totalPrice для ответа
        int totalDuration = 0;
        double totalPrice = 0.0;
        if (created.getItems() != null) {
            for (var item : created.getItems()) {
                var service = item.getService();
                if (service.getType() != null && service.getType().name().equals("FIXED")) {
                    if (service.getFixedDetails() != null) {
                        totalDuration += Optional.ofNullable(service.getFixedDetails().getDurationMinutes()).orElse(0);
                        totalPrice += Optional.ofNullable(service.getFixedDetails().getPrice()).orElse(0.0);
                    }
                } else {
                    if (item.getVariableDetails() != null) {
                        for (var v : item.getVariableDetails()) {
                            totalDuration += Optional.ofNullable(v.getDurationMinutes()).orElse(0);
                            totalPrice += Optional.ofNullable(v.getPrice()).orElse(0.0);
                        }
                    }
                }
            }
        }

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();

        return ResponseEntity.created(location)
                .body(mapper.toDto(created, totalDuration, totalPrice));
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponseDto> getById(@PathVariable Long id) {
        Appointment a = appointmentService.getById(id);
        return ResponseEntity.ok(mapper.toDto(a));
    }

    @GetMapping("/masters/{masterId}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getByMaster(@PathVariable Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        List<AppointmentResponseDto> list = appointmentService.getByMaster(masterId).stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/clients/{clientId}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getByClient(@PathVariable String clientId) {
        Assert.hasText(clientId, "clientId must not be empty");
        List<AppointmentResponseDto> list = appointmentService.getByClient(clientId).stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable Long id,
                                                         @RequestParam(value = "clientId", required = false) String clientId) {
        Appointment cancelled = appointmentService.cancel(id, clientId);
        return ResponseEntity.ok(mapper.toDto(cancelled));
    }

    @PostMapping("/appointments/{id}/confirm")
    public ResponseEntity<AppointmentResponseDto> confirm(@PathVariable Long id) {
        Appointment confirmed = appointmentService.confirm(id);
        return ResponseEntity.ok(mapper.toDto(confirmed));
    }
}
