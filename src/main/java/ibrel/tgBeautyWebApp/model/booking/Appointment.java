package ibrel.tgBeautyWebApp.model.booking;

import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointments_master_id", columnList = "master_id"),
        @Index(name = "idx_appointments_slot_id", columnList = "slot_id"),
        @Index(name = "idx_appointments_client_id", columnList = "client_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", length = 200, nullable = false)
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    @JsonIgnore
    private Master master;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private WorkSlot slot;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentServiceItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}
