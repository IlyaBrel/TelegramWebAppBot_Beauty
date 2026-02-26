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
    private Long userId;
    private Long masterId;
    private Long slotId;
    private List<ServiceSummaryDto> services;
    private Integer totalDuration;
    private Double totalPrice;
    private String status;
    private OffsetDateTime createdAt;
}
