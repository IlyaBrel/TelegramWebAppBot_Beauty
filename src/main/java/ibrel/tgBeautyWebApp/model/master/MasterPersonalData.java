package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_personal_data")
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
    private String instUserId;
    private String description;
    private String phone;
    private Integer experienceYears;
    private Integer completedJobs;

    @OneToOne
    @JoinColumn(name = "master_id")
    private Master master;
}

