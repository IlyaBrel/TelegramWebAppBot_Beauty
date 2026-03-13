package ibrel.tgBeautyWebApp.web.controller.admin;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.dto.user.UserDto;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.service.user.UserService;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import ibrel.tgBeautyWebApp.web.mapper.appointment.AppointmentMapper;
import ibrel.tgBeautyWebApp.web.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService        userService;
    private final AppointmentService appointmentService;
    private final AppointmentMapper  appointmentMapper;
    private final UserMapper         userMapper;

    @GetMapping("/pending")
    public ResponseEntity<List<UserDto>> pending() {
        return ResponseEntity.ok(
                userService.listPendingActivation().stream().map(userMapper::toDto).toList());
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(
                userService.findAll().stream().map(userMapper::toDto).toList());
    }

    @GetMapping("/{telegramId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long telegramId) {
        return userService.findByTelegramId(telegramId)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{telegramId}/activate")
    public ResponseEntity<UserDto> activate(@PathVariable Long telegramId,
                                            @RequestParam Long adminTelegramId) {
        return ResponseEntity.ok(userMapper.toDto(
                userService.activateUser(telegramId, adminTelegramId)));
    }

    @PostMapping("/create")
    public ResponseEntity<UserDto> createByAdmin(@RequestBody UserTG user,
                                                 @RequestParam Long adminTelegramId) {
        return ResponseEntity.status(201).body(userMapper.toDto(
                userService.createByAdmin(user, adminTelegramId)));
    }

    @PostMapping("/{telegramId}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long telegramId,
                                           @RequestParam UserRole role,
                                           @RequestParam Long adminTelegramId) {
        userService.changeRole(telegramId, role, adminTelegramId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{telegramId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long telegramId,
                                              @RequestBody UserTG dto,
                                              @RequestParam Long adminTelegramId) {
        return ResponseEntity.ok(userMapper.toDto(
                userService.updateUser(telegramId, dto, adminTelegramId)));
    }

    @PostMapping("/{telegramId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long telegramId,
                                           @RequestParam Long adminTelegramId) {
        userService.changeActivity(telegramId, false, adminTelegramId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{telegramId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long telegramId) {
        userService.deleteByTelegramId(telegramId);
        return ResponseEntity.noContent().build();
    }

    /** Записи пользователя — clientId теперь Long */
    @GetMapping("/{clientTelegramId}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getUserAppointments(
            @PathVariable Long clientTelegramId) {
        return ResponseEntity.ok(
                appointmentService.getByClient(clientTelegramId)
                        .stream().map(appointmentMapper::toDto).toList());
    }
}