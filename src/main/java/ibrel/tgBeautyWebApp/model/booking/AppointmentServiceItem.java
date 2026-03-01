package ibrel.tgBeautyWebApp.model.booking;

import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import ibrel.tgBeautyWebApp.model.master.service.VariableServiceDetails;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "appointment_service_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MasterServiceWork service;

    @ManyToMany
    @JoinTable(name = "appointment_item_variable_details",
            joinColumns = @JoinColumn(name = "appointment_item_id"),
            inverseJoinColumns = @JoinColumn(name = "variable_detail_id"))
    private List<VariableServiceDetails> variableDetails;
}
