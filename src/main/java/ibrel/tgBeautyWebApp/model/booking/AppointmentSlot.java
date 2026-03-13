package ibrel.tgBeautyWebApp.model.booking;

import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import jakarta.persistence.*;
import lombok.*;

/**
 * Связь заказа со слотом.
 * Один заказ может занимать несколько слотов подряд.
 * slotOrder — порядковый номер слота в заказе (0, 1, 2...)
 */
@Entity
@Table(name = "appointment_slots",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_appointment_slot",
                columnNames = {"appointment_id", "slot_id"}
        ),
        indexes = {
                @Index(name = "idx_apt_slot_appointment", columnList = "appointment_id"),
                @Index(name = "idx_apt_slot_slot",        columnList = "slot_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private WorkSlot slot;

    /** Порядковый номер слота в заказе (0 = первый) */
    @Column(name = "slot_order", nullable = false)
    private Integer slotOrder;
}