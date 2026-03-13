package ibrel.tgBeautyWebApp.model.booking;

import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Заказ (запись пользователя к мастеру).
 *
 * Поддерживает несколько слотов: если суммарная длительность услуг
 * превышает один слот, заказ занимает несколько подряд идущих слотов.
 * Все занятые слоты хранятся в {@link AppointmentSlot}.
 */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_apt_master_id",  columnList = "master_id"),
        @Index(name = "idx_apt_client_id",  columnList = "client_telegram_id"),
        @Index(name = "idx_apt_status",     columnList = "status"),
        @Index(name = "idx_apt_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** telegramId клиента */
    @Column(name = "client_telegram_id", nullable = false)
    private Long clientTelegramId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    @JsonIgnore
    private Master master;

    /**
     * Все слоты, занятые этим заказом (один или несколько подряд).
     * Первый слот = начало записи.
     */
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("slotOrder ASC")
    private List<AppointmentSlot> slots;

    /** Выбранные услуги и их детали */
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentServiceItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    /** Суммарная длительность всех услуг (мин) — кешируется при создании */
    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    /** Суммарная стоимость — кешируется при создании */
    @Column(name = "total_price")
    private Double totalPrice;

    /**
     * ID сертификата, использованного при записи.
     * null — оплата стандартная.
     */
    @Column(name = "certificate_id")
    private Long certificateId;

    // В модель Appointment добавить:
    @Column(name = "paid_by_certificate")
    @Builder.Default
    private Boolean paidByCertificate = false;

    @Column(name = "certificate_code", length = 20)
    private String certificateCode;

    /** Причина отклонения/отмены (заполняется мастером или системой) */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    /**
     * true — напоминание уже отправлено пользователю.
     * Используется планировщиком чтобы не дублировать уведомления.
     */
    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    /**
     * true — запрос на отзыв уже отправлен после завершения.
     */
    @Column(name = "review_request_sent")
    @Builder.Default
    private Boolean reviewRequestSent = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public enum Status {
        PENDING,    // ожидает подтверждения мастером
        CONFIRMED,  // подтверждён мастером
        REJECTED,   // отклонён мастером (с причиной)
        CANCELLED,  // отменён пользователем
        COMPLETED   // завершён
    }
}