package ibrel.tgBeautyWebApp.web.controller;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

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
}
