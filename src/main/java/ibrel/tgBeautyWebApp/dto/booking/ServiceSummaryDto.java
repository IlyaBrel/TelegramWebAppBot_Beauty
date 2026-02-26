package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceSummaryDto {
    private Long id;
    private String name;
    private String type; // FIXED or VARIABLE
    private Integer durationMinutes;
    private Double price;
}
