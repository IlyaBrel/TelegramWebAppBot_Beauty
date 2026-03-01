package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDto {
    private Long id;
    private Boolean active;
    private String firstName;
    private String lastName;
    private String description;
    private String phone;
    private Integer experienceYears;
    private Integer completedJobs;
    private String city;
    private String street;
    private String house;
    private String floor;
    private String apartment;
    private String placeOnTheMap;
}
