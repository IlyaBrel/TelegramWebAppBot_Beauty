package ibrel.tgBeautyWebApp.dto.booking;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentAvailabilityResponseDto {
    private Boolean available;      // слот свободен и услуги помещаются
    private Boolean slotFree;       // нет других записей
    private Boolean fitsInSlot;     // по длительности помещается
    private String reason;          // если !available — почему
}
