package ibrel.tgBeautyWebApp.model.certificate;

import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Подарочный сертификат от мастера пользователю.
 * Мастер создаёт сертификат, выбирая услуги из своего каталога.
 * Пользователь использует сертификат при записи — выбирает только время.
 */
@Entity
@Table(name = "certificates", indexes = {
        @Index(name = "idx_cert_code",              columnList = "code",              unique = true),
        @Index(name = "idx_cert_recipient",         columnList = "recipient_telegram_id"),
        @Index(name = "idx_cert_master_id",         columnList = "master_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Уникальный код сертификата (UUID или короткий код) */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    /** telegramId получателя */
    @Column(name = "recipient_telegram_id", nullable = false)
    private Long recipientTelegramId;

    /** Услуги, включённые в сертификат */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "certificate_services",
            joinColumns        = @JoinColumn(name = "certificate_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<MasterServiceWork> services;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.ACTIVE;

    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;

    /** ID заказа, при котором был использован сертификат */
    @Column(name = "used_in_appointment_id")
    private Long usedInAppointmentId;

    public enum CertificateStatus {
        ACTIVE,   // активен, можно использовать
        USED,     // уже использован
        EXPIRED,  // истёк срок действия
        CANCELLED // отменён мастером/администратором
    }
}