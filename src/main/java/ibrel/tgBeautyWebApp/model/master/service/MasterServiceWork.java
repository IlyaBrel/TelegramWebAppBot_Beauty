package ibrel.tgBeautyWebApp.model.master.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.enums.MasterServiceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private MasterServiceType type; // внешний enum

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    @JsonIgnore
    private Master master;

    @OneToOne(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FixedServiceDetails fixedDetails;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariableServiceDetails> variableDetails;

    // helper methods
    public void addVariableDetail(VariableServiceDetails v) {
        if (variableDetails == null) variableDetails = new ArrayList<>();
        variableDetails.add(v);
        v.setService(this);
    }
    public void removeVariableDetail(VariableServiceDetails v) {
        if (variableDetails != null) {
            variableDetails.remove(v);
            v.setService(null);
        }
    }
    public void setFixedDetailsBidirectional(FixedServiceDetails fd) {
        this.fixedDetails = fd;
        if (fd != null) fd.setService(this);
    }
}



