package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearestSlotsRequestDto {

    @NotNull
    private Long masterId;

    @NotEmpty
    private List<ServiceSelectionDto> services;

    // сколько слотов вернуть
    @Builder.Default
    private Integer limit = 20;
}
