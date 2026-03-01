package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceSelectionDto {

    @NotNull
    private Long serviceId;

    private List<Long> variableDetailIds;
}
