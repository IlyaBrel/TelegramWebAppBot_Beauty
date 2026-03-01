package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableServiceDetailsDto {
    private Long id;
    private String factorName;
    private String factorValue;
    private Integer durationMinutes;
    private Double price;
    private String description;
}
