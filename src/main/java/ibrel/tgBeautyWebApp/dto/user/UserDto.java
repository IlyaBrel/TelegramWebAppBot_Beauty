package ibrel.tgBeautyWebApp.dto.user;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long   id;
    private Long   telegramId;
    private String username;
    private String firstName;
    private String lastName;
    private String role;     // USER, ADMIN
    private Boolean active;
    private LocalDateTime createdAt;
}
