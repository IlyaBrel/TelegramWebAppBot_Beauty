package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCalcResponseDto {

    private Integer totalDurationMinutes;
    private Double totalPrice;
    private Boolean fitsInSlot;
}
