package ibrel.tgBeautyWebApp.service.booking;

import ibrel.tgBeautyWebApp.dto.booking.*;
import java.util.List;

public interface AppointmentCalcService {

    AppointmentCalcResponseDto calculate(AppointmentCalcRequestDto request);

    AppointmentAvailabilityResponseDto checkAvailability(AppointmentCalcRequestDto request);

    List<WorkSlotShortDto> findNearestSlots(NearestSlotsRequestDto request);

    OptimalSlotResponseDto findOptimalSlot(NearestSlotsRequestDto request);
}
