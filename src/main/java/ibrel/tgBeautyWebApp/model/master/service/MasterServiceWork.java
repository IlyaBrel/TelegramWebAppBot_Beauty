package ibrel.tgBeautyWebApp.model.master.service;

import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.enums.MasterServiceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "master_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterServiceWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private MasterServiceType type; // FIXED или VARIABLE

    @ManyToOne
    @JoinColumn(name = "master_id")
    private Master master;

    @OneToOne(mappedBy = "service", cascade = CascadeType.ALL)
    private FixedServiceDetails fixedDetails;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<VariableServiceDetails> variableDetails;

}


