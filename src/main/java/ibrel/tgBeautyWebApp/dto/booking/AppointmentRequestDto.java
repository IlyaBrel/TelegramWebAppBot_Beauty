package ibrel.tgBeautyWebApp.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequestDto {
    @NotNull(message = "userId must not be null")
    private Long userId;
    @NotNull(message = "masterId must not be null")
    private Long masterId;
    @NotNull(message = "slotId must not be null")
    private Long slotId;
    @NotEmpty(message = "serviceIds must not be empty")
    private List<@NotNull(message = "serviceId must not be null") Long> serviceIds;
    private List<VariableServiceSelectionDto> variableSelections;
}
