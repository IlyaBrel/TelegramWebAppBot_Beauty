package ibrel.tgBeautyWebApp.model.certificate;

import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Предложение сертификата — шаблон, который мастер или admin выставляет публично.
 * Пользователь выбирает оффер и создаёт CertificatePurchaseRequest (оплата офлайн).
 *
 * Пример: "Маникюр + покрытие — 2500 ₽, действует 60 дней"
 */
@Entity
@Table(name = "certificate_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Мастер, которому принадлежит предложение */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    /** Название, например "Комплекс — маникюр + покрытие" */
    @Column(nullable = false, length = 200)
    private String title;

    /** Описание (опционально) */
    @Column(length = 1000)
    private String description;

    /**
     * Услуги, входящие в сертификат.
     * При записи пользователь не выбирает услуги — они зафиксированы в оффере.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "certificate_offer_services",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<MasterServiceWork> services;

    /** Цена предложения (может быть со скидкой относительно суммы услуг по отдельности) */
    @Column(nullable = false)
    private Double price;

    /** Срок действия купленного сертификата в днях (null = бессрочно) */
    @Column(name = "valid_days")
    private Integer validDays;

    /** Активно ли предложение (мастер/admin может скрыть) */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** Кто создал оффер (telegramId мастера или admin) */
    @Column(name = "created_by_telegram_id")
    private Long createdByTelegramId;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /** Доступно ли предложение для покупки */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(active);
    }
}