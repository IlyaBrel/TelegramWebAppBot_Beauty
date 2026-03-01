package ibrel.tgBeautyWebApp.model.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "masters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean active;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @OneToOne(mappedBy = "master", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MasterPersonalData personalData;

    @OneToOne(mappedBy = "master", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MasterAddress address;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork> services;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<WorkSlot> slots;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MasterReview> reviews;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MasterWorkExample> works;

    @OneToMany(mappedBy = "master")
    @JsonIgnore
    private List<Appointment> appointments;

    // helper setters to keep bidirectional consistency
    public void setPersonalDataBidirectional(MasterPersonalData pd) {
        this.personalData = pd;
        if (pd != null) pd.setMaster(this);
    }

    public void setAddressBidirectional(MasterAddress addr) {
        this.address = addr;
        if (addr != null) addr.setMaster(this);
    }
}
