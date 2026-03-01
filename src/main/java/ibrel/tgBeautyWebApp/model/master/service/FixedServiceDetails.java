package ibrel.tgBeautyWebApp.model.master.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String description;

    @OneToOne
    @JoinColumn(name = "master_service_id")
    @JsonIgnore
    private MasterServiceWork service;
}



