package ibrel.tgBeautyWebApp.web.controller.booking;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentCreateRequestDto;
import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import ibrel.tgBeautyWebApp.web.mapper.appointment.AppointmentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper  mapper;

    /** Создать запись */
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponseDto> create(
            @Valid @RequestBody AppointmentCreateRequestDto req) {

        Appointment created = appointmentService.createAppointment(
                req.getClientTelegramId(),
                req.getMasterId(),
                req.getStartSlotId(),
                req.getServices(),
                req.getPromoCode(),
                req.getBonusAmountToUse()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();

        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    /** Получить запись по id */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(appointmentService.getById(id)));
    }

    /** Все записи мастера */
    @GetMapping("/masters/{masterId}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getByMaster(@PathVariable Long masterId) {
        List<AppointmentResponseDto> list = appointmentService.getByMaster(masterId)
                .stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    /** Все записи клиента */
    @GetMapping("/clients/{clientTelegramId}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getByClient(
            @PathVariable Long clientTelegramId) {
        List<AppointmentResponseDto> list = appointmentService.getByClient(clientTelegramId)
                .stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    /** Отменить запись (клиент) */
    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(
            @PathVariable Long id,
            @RequestParam Long clientTelegramId) {
        Appointment cancelled = appointmentService.cancelByClient(id, clientTelegramId);
        return ResponseEntity.ok(mapper.toDto(cancelled));
    }

    /** Подтвердить запись (мастер) */
    @PostMapping("/appointments/{id}/confirm")
    public ResponseEntity<AppointmentResponseDto> confirm(
            @PathVariable Long id,
            @RequestParam Long masterTelegramId) {
        Appointment confirmed = appointmentService.confirmByMaster(id, masterTelegramId);
        return ResponseEntity.ok(mapper.toDto(confirmed));
    }

    /** Отклонить запись (мастер) */
    @PostMapping("/appointments/{id}/reject")
    public ResponseEntity<AppointmentResponseDto> reject(
            @PathVariable Long id,
            @RequestParam Long masterTelegramId,
            @RequestParam(required = false) String reason) {
        Appointment rejected = appointmentService.rejectByMaster(id, masterTelegramId, reason);
        return ResponseEntity.ok(mapper.toDto(rejected));
    }

    /** Завершить запись (мастер) */
    @PostMapping("/appointments/{id}/complete")
    public ResponseEntity<AppointmentResponseDto> complete(
            @PathVariable Long id,
            @RequestParam Long masterTelegramId) {
        Appointment completed = appointmentService.complete(id, masterTelegramId);
        return ResponseEntity.ok(mapper.toDto(completed));
    }
}