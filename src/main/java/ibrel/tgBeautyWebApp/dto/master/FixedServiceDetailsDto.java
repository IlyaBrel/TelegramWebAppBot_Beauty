package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedServiceDetailsDto {
    private Long id;
    private Integer durationMinutes;
    private Double price;
    private String description;
}
