package ibrel.tgBeautyWebApp.model.master.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "variable_service_details", indexes = {
        @Index(name = "idx_var_detail_service_id", columnList = "master_service_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableServiceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String factorName;

    @Column(nullable = false, length = 200)
    private String factorValue;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_service_id")
    @JsonIgnore
    private MasterServiceWork service;
}