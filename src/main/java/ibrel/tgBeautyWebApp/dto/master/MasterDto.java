package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDto {
    private Long id;
    private Boolean active;
    private String imageUrl;
    private MasterPersonalDataDto personalData;
    private MasterAddressDto address;
    private Integer experienceYears;
    private Integer completedJobs;
    private Double averageRating;
}
