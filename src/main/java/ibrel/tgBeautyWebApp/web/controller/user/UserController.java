package ibrel.tgBeautyWebApp.web.controller.user;

import ibrel.tgBeautyWebApp.dto.user.UserDto;
import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.service.user.UserService;
import ibrel.tgBeautyWebApp.web.mapper.user.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper  userMapper;

    /**
     * Сохранение / обновление пользователя при старте бота.
     * Telegram-бот вызывает этот метод при каждом /start.
     */
    @PostMapping("/save")
    public ResponseEntity<UserDto> save(@Valid @RequestBody UserTG userTG) {
        UserTG saved = userService.save(userTG);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{telegramId}").buildAndExpand(saved.getTelegramId()).toUri();
        return ResponseEntity.created(location).body(userMapper.toDto(saved));
    }

    /**
     * Получить профиль пользователя по telegramId.
     * Используется ботом для отображения данных пользователя.
     */
    @GetMapping("/{telegramId}")
    public ResponseEntity<UserDto> getByTelegramId(@PathVariable Long telegramId) {
        return userService.findByTelegramId(telegramId)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Проверка — зарегистрирован ли пользователь.
     * Позволяет боту быстро определить, нужно ли показывать онбординг.
     */
    @GetMapping("/{telegramId}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable Long telegramId) {
        return ResponseEntity.ok(userService.exists(telegramId));
    }

    /**
     * Общее количество зарегистрированных пользователей.
     * Используется в дашборде и статистике.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(userService.countUsers());
    }

    /**
     * Проверка наличия хотя бы одного администратора в системе.
     * Используется при первичной настройке приложения.
     */
    @GetMapping("/has-admin")
    public ResponseEntity<Boolean> hasAdmin() {
        return ResponseEntity.ok(userService.hasAnyAdmin());
    }
}
