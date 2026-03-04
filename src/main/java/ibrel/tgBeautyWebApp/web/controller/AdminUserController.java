package ibrel.tgBeautyWebApp.web.controller;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.service.UserService;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import ibrel.tgBeautyWebApp.web.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final AppointmentMapper mapper;

    // Список пользователей, ожидающих активации
    @GetMapping("/pending")
    public ResponseEntity<List<UserTG>> pending() {
        return ResponseEntity.ok(userService.listPendingActivation());
    }

    // Активировать пользователя (adminTelegramId — telegramId админа, который выполняет действие)
    @PostMapping("/{telegramId}/activate")
    public ResponseEntity<UserTG> activate(@PathVariable Long telegramId, @RequestParam Long adminTelegramId) {
        UserTG activated = userService.activateUser(telegramId, adminTelegramId);
        return ResponseEntity.ok(activated);
    }

    // Создать пользователя от имени админа (активный сразу)
    @PostMapping("/create")
    public ResponseEntity<UserTG> createByAdmin(@RequestBody UserTG user, @RequestParam Long adminTelegramId) {
        UserTG created = userService.createByAdmin(user, adminTelegramId);
        return ResponseEntity.status(201).body(created);
    }

    // Сменить роль пользователя
    @PostMapping("/{telegramId}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long telegramId,
                                           @RequestParam UserRole role,
                                           @RequestParam Long adminTelegramId) {
        userService.changeRole(telegramId, role, adminTelegramId);
        return ResponseEntity.ok().build();
    }

    // Все пользователи (не только pending)
    @GetMapping
    public ResponseEntity<List<UserTG>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // Получить одного пользователя
    @GetMapping("/{telegramId}")
    public ResponseEntity<UserTG> getUser(@PathVariable Long telegramId) {
        return userService.findByTelegramId(telegramId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить пользователя
    @PutMapping("/{telegramId}")
    public ResponseEntity<UserTG> updateUser(@PathVariable Long telegramId,
                                             @RequestBody UserTG dto,
                                             @RequestParam Long adminTelegramId) {
        UserTG updated = userService.updateUser(telegramId, dto, adminTelegramId);
        return ResponseEntity.ok(updated);
    }

    // Деактивировать пользователя
    @PostMapping("/{telegramId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long telegramId,
                                           @RequestParam Long adminTelegramId) {
        userService.changeActivity(telegramId, false, adminTelegramId);
        return ResponseEntity.ok().build();
    }

    // Удалить пользователя
    @DeleteMapping("/{telegramId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long telegramId) {
        userService.deleteByTelegramId(telegramId);
        return ResponseEntity.noContent().build();
    }

    // Заказы пользователя
    @GetMapping("/{clientId}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getUserAppointments(
            @PathVariable String clientId) {
        List<AppointmentResponseDto> list = appointmentService.getByClient(clientId)
                .stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(list);
    }
}
