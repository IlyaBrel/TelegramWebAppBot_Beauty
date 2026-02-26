package ibrel.tgBeautyWebApp.model.master.service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fixed_service_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedServiceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double price;
    private Integer durationMinutes;

    @OneToOne
    @JoinColumn(name = "master_service_id")
    private MasterService service; // ⚡ связь с MasterService
}


