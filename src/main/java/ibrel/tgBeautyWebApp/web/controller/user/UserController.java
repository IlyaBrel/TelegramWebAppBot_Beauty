package ibrel.tgBeautyWebApp.web.controller.user;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
