package ibrel.tgBeautyWebApp.dto.catalog;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmenityDto {
    private Long    id;
    private String  name;
    private String  description;
    private String  icon;
    private Boolean active;
}