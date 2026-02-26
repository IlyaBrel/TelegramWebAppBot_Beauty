package ibrel.tgBeautyWebApp.model;

import ibrel.tgBeautyWebApp.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserTG {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
    //убрать в будующем
    private String languageCode;
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    private Boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
