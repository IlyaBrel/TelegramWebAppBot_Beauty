package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSlotDto {
    private Long id;
    private Long masterId;
    private String dayOfWeek; // MONDAY, TUESDAY...
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;
}
