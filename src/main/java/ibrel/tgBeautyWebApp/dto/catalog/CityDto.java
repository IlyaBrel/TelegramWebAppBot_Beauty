package ibrel.tgBeautyWebApp.dto.catalog;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityDto {
    private Long    id;
    private String  name;
    private String  region;
    private Boolean active;
}