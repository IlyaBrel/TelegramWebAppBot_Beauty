package ibrel.tgBeautyWebApp.model.master.service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "variable_service_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableServiceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String factorName;
    private String factorValue;
    private Double price;
    private Integer durationMinutes;

    @ManyToOne
    @JoinColumn(name = "master_service_id")
    private MasterService service;
}


