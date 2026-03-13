package ibrel.tgBeautyWebApp.model.master;

import ibrel.tgBeautyWebApp.model.booking.AppointmentSlot;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "work_slots", indexes = {
        @Index(name = "idx_slot_master_id",    columnList = "master_id"),
        @Index(name = "idx_slot_day_of_week",  columnList = "day_of_week")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * День недели: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
     */
    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    @JsonIgnore
    private Master master;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AppointmentSlot> appointmentSlots;
}