package ibrel.tgBeautyWebApp.model.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    /**
     * День недели в формате Enum.name() — MONDAY, TUESDAY и т.д.
     * Храним как String для простоты запросов.
     */
    private String dayOfWeek;

    /**
     * Время начала и конца слота (локальное время, без даты).
     * Пример: startTime = 13:10, endTime = 14:10
     */
    private LocalTime startTime;
    private LocalTime endTime;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    @JsonIgnore
    private Master master;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Appointment> appointments;
}
