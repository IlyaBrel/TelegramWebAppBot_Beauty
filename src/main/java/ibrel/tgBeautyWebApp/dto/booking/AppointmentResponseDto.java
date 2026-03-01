package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDto {

    private Long id;
    private String clientId;
    private Long masterId;
    private Long slotId;
    private String status;

    private List<AppointmentItemDto> items;

    private Integer totalDurationMinutes;
    private Double totalPrice;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
