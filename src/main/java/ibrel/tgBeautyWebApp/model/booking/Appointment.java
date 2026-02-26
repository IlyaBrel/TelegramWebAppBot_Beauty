package ibrel.tgBeautyWebApp.model.booking;

import ibrel.tgBeautyWebApp.model.UserTG;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Когда была создана запись
    private LocalDateTime createdAt = LocalDateTime.now();

    // Итоговое время всех процедур (в минутах)
    private Integer totalDuration;

    // Итоговая цена всех процедур
    private Double totalPrice;

    // Клиент
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserTG user;

    // Мастер
    @ManyToOne
    @JoinColumn(name = "master_id")
    private Master master;

    // Выбранные услуги (их может быть несколько)
    @ManyToMany
    @JoinTable(
            name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<MasterServiceWork> services;

    // Выбранный слот времени
    @ManyToOne
    @JoinColumn(name = "slot_id")
    private WorkSlot slot;

    // Статус записи
    private String status; // BOOKED, COMPLETED, CANCELLED
}
