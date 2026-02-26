package ibrel.tgBeautyWebApp.model.master;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    @Builder.Default
    private Boolean active = true;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "master", cascade = CascadeType.ALL)
    private MasterAddress address;

    @OneToOne(mappedBy = "master", cascade = CascadeType.ALL)
    private MasterPersonalData personalData;
    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<MasterServiceWork> services;
    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<MasterReview> reviews;
    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<MasterWorkExample> works;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<WorkSlot> workSlots;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

}
