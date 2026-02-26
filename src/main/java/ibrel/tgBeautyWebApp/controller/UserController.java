package ibrel.tgBeautyWebApp.controller;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // 🔹 Создание пользователя
    @PostMapping("/save")
    public ResponseEntity<UserTG> save(@RequestBody UserTG userTG) {
        return ResponseEntity.ok(service.save(userTG));
    }

    // 🔹 Получение пользователя по Telegram ID
    @GetMapping("/{telegramId}")
    public ResponseEntity<UserTG> getByTelegramId(@PathVariable Long telegramId) {
        return service.findByTelegramId(telegramId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Получение всех пользователей
    @GetMapping("/all")
    public List<UserTG> getAll() {
        return service.findAll();
    }

    // 🔹 Удаление пользователя
    @DeleteMapping("/{telegramId}")
    public ResponseEntity<Void> delete(@PathVariable Long telegramId) {
        service.deleteByTelegramId(telegramId);
        return ResponseEntity.noContent().build();
    }

    // 🔹 Изменение активности
    @PatchMapping("/{telegramId}/activity")
    public ResponseEntity<Void> updateActivity(@PathVariable Long telegramId,
                                               @RequestParam boolean active) {
        service.changeActivity(telegramId, active);
        return ResponseEntity.ok().build();
    }

    // 🔹 Изменение роли
    @PatchMapping("/{telegramId}/role")
    public ResponseEntity<Void> updateRole(@PathVariable Long telegramId,
                                           @RequestParam UserRole role) {
        service.changeRole(telegramId, role);
        return ResponseEntity.ok().build();
    }

    // 🔹 Обновление данных пользователя
    @PutMapping("/{telegramId}")
    public ResponseEntity<UserTG> updateUser(@PathVariable Long telegramId,
                                             @RequestBody UserTG updatedData) {
        return ResponseEntity.ok(service.updateUser(telegramId, updatedData));
    }

    // 🔹 Проверка существования
    @GetMapping("/{telegramId}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable Long telegramId) {
        return ResponseEntity.ok(service.exists(telegramId));
    }

    // 🔹 Подсчёт пользователей
    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(service.countUsers());
    }
}
