package ibrel.tgBeautyWebApp.model.master.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_service_id")
    @JsonIgnore
    private MasterServiceWork service;
}



