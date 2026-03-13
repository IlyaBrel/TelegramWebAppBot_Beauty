package ibrel.tgBeautyWebApp.model;

import ibrel.tgBeautyWebApp.model.bonus.BonusAccount;
import ibrel.tgBeautyWebApp.model.enums.UserRole;
import ibrel.tgBeautyWebApp.model.master.City;
import ibrel.tgBeautyWebApp.model.task.UserTaskProgress;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_telegram_id", columnList = "telegram_id"),
        @Index(name = "idx_user_role",        columnList = "role"),
        @Index(name = "idx_user_active",      columnList = "active"),
        @Index(name = "idx_user_invite_code", columnList = "invite_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"invitedBy", "bonusAccount", "taskProgress"})
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

    // ── Баллы и реферальная система ──────────────────────────────────────────

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 0;

    /** 6-символьный уникальный код приглашения */
    @Column(name = "invite_code", length = 6, unique = true)
    private String inviteCode;

    /** Кто пригласил этого пользователя */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private UserTG invitedBy;

    /** Сколько пользователей зарегистрировалось по этому коду */
    @Column(name = "invited_users_count", nullable = false)
    @Builder.Default
    private Integer invitedUsersCount = 0;

    /** Сколько из приглашённых завершили хотя бы один заказ */
    @Column(name = "invited_users_orders_count", nullable = false)
    @Builder.Default
    private Integer invitedUsersOrdersCount = 0;

    // ── Предпочтения ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_city_id")
    private City favoriteCity;

    // ── Связи ─────────────────────────────────────────────────────────────────

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BonusAccount bonusAccount;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTaskProgress> taskProgress;
}