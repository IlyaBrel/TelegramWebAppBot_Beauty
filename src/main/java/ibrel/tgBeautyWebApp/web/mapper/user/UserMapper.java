package ibrel.tgBeautyWebApp.web.mapper.user;

import ibrel.tgBeautyWebApp.dto.user.UserDto;
import ibrel.tgBeautyWebApp.model.UserTG;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(UserTG u) {
        if (u == null) return null;
        return UserDto.builder()
                .id(u.getId())
                .telegramId(u.getTelegramId())
                .username(u.getUsername())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .active(u.getActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
