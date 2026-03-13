package ibrel.tgBeautyWebApp.model.master.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.service.enums.MasterServiceType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "master_services", indexes = {
        @Index(name = "idx_service_master_id", columnList = "master_id"),
        @Index(name = "idx_service_type",      columnList = "type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterServiceWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    /** Фото услуги */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MasterServiceType type;

    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    @JsonIgnore
    private Master master;

    @OneToOne(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FixedServiceDetails fixedDetails;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariableServiceDetails> variableDetails;

    // ── Helper методы ─────────────────────────────────────────────────────────

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