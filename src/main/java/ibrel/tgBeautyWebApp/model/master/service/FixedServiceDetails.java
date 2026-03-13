package ibrel.tgBeautyWebApp.model.master.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(length = 1000)
    private String description;

    @OneToOne
    @JoinColumn(name = "master_service_id")
    @JsonIgnore
    private MasterServiceWork service;
}