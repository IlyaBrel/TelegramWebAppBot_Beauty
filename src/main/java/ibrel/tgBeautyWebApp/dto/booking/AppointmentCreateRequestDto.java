package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreateRequestDto {

    @NotNull
    private String clientId;

    @NotNull
    private Long masterId;

    @NotNull
    private Long slotId;

    @NotEmpty
    private List<ServiceSelectionDto> services;
}
