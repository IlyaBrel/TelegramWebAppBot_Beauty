package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterPersonalDataDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String description;
    private String phone;
    private Integer experienceYears;
    private Integer completedJobs;
    private String instUserId;
}
