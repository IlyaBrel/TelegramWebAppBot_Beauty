package ibrel.tgBeautyWebApp.model.certificate;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Заявка пользователя на покупку сертификата по офферу мастера.
 *
 * Жизненный цикл:
 *   PENDING  → пользователь создал заявку, ждёт подтверждения от мастера
 *   APPROVED → мастер подтвердил оплату → автоматически выпускается Certificate
 *   REJECTED → мастер отклонил заявку (с причиной)
 *   CANCELLED → пользователь сам отменил заявку
 */
@Entity
@Table(name = "certificate_purchase_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificatePurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Предложение, которое хочет купить пользователь */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private CertificateOffer offer;

    /** TelegramId покупателя */
    @Column(name = "buyer_telegram_id", nullable = false)
    private Long buyerTelegramId;

    /** Статус заявки */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PurchaseStatus status = PurchaseStatus.PENDING;

    /** Причина отклонения (заполняет мастер при REJECTED) */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * Ссылка на выпущенный сертификат.
     * Заполняется автоматически при переходе в статус APPROVED.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    /** Сообщение/комментарий от покупателя (опционально) */
    @Column(name = "buyer_comment", length = 500)
    private String buyerComment;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ── Статус ────────────────────────────────────────────────────────────────

    public enum PurchaseStatus {
        PENDING,    // ждёт подтверждения мастером
        APPROVED,   // мастер одобрил → сертификат выпущен
        REJECTED,   // мастер отклонил
        CANCELLED   // пользователь отменил
    }
}