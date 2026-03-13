package ibrel.tgBeautyWebApp.model;

import ibrel.tgBeautyWebApp.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_telegram_id", columnList = "telegram_id"),
        @Index(name = "idx_user_role",        columnList = "role"),
        @Index(name = "idx_user_active",      columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserTG {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;

    private String username;
    private String firstName;
    private String lastName;
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Builder.Default
    private Boolean active = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    /** telegramId админа, который активировал/создал пользователя */
    private Long createdByAdminTelegramId;

    /** Первый администратор системы — нельзя удалить или разжаловать */
    @Builder.Default
    private Boolean isInitialAdmin = false;
}