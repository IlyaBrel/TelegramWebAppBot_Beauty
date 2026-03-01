package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCalcRequestDto {

    @NotNull
    private Long masterId;

    @NotNull
    private Long slotId;

    @NotEmpty
    private List<ServiceSelectionDto> services;
}
