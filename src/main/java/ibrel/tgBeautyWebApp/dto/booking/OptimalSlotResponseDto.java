package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimalSlotResponseDto {
    private WorkSlotShortDto slot;
    private Boolean fitsInSlot; // на всякий случай
}
