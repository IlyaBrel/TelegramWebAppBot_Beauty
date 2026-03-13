package ibrel.tgBeautyWebApp.web.controller.booking;

import ibrel.tgBeautyWebApp.dto.booking.*;
import ibrel.tgBeautyWebApp.service.booking.AppointmentCalcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentCalcController {

    private final AppointmentCalcService calcService;

    @PostMapping("/calc")
    public ResponseEntity<AppointmentCalcResponseDto> calculate(
            @Valid @RequestBody AppointmentCalcRequestDto req) {
        return ResponseEntity.ok(calcService.calculate(req));
    }

    @PostMapping("/check-availability")
    public ResponseEntity<AppointmentAvailabilityResponseDto> checkAvailability(
            @Valid @RequestBody AppointmentCalcRequestDto req) {
        return ResponseEntity.ok(calcService.checkAvailability(req));
    }

    @PostMapping("/nearest-slots")
    public ResponseEntity<List<WorkSlotShortDto>> findNearestSlots(
            @Valid @RequestBody NearestSlotsRequestDto req) {
        return ResponseEntity.ok(calcService.findNearestSlots(req));
    }

    @PostMapping("/optimal-slot")
    public ResponseEntity<OptimalSlotResponseDto> findOptimalSlot(
            @Valid @RequestBody NearestSlotsRequestDto req) {
        return ResponseEntity.ok(calcService.findOptimalSlot(req));
    }
}