package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "master_personal_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"master_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterPersonalData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(length = 2000)
    private String description;

    private String phone;
    private Integer experienceYears;
    private Integer completedJobs;

    private String instUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", unique = true)
    @JsonIgnore
    private Master master;
}
