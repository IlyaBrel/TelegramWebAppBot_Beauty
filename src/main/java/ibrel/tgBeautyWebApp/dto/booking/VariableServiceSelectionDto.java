package ibrel.tgBeautyWebApp.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableServiceSelectionDto {
    @NotNull(message = "serviceId must not be null")
    private Long serviceId;
    private String factorName;
    private String factorValue;
}
