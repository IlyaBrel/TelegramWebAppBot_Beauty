package ibrel.tgBeautyWebApp.model.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "work_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dayOfWeek; // MONDAY, TUESDAY...

    private LocalTime startTime; // например 13:10
    private LocalTime endTime;   // например 14:10

    @ManyToOne
    @JoinColumn(name = "master_id")
    private Master master;

    @OneToMany(mappedBy = "slot")
    private List<Appointment> appointments;

}
