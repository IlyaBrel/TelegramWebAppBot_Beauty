package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentItemDto {
    private Long serviceId;
    private List<Long> variableDetailIds;
}
