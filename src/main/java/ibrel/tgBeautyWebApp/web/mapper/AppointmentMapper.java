package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentRequestDto;
import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.dto.booking.ServiceSummaryDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AppointmentMapper {

    public AppointmentRequestDto toRequest(AppointmentRequestDto dto) {
        if (dto == null) return null;
        return AppointmentRequestDto.builder()
                .userTelegramId(dto.getUserTelegramId())
                .masterId(dto.getMasterId())
                .slotId(dto.getSlotId())
                .variableSelections(dto.getVariableSelections())
                .build();
    }

    public AppointmentResponseDto toDto(Appointment appt) {
        if (appt == null) return null;
        List<ServiceSummaryDto> services = appt.getServices() == null ? List.of() :
                appt.getServices().stream().map(this::toServiceSummary).collect(Collectors.toList());

        OffsetDateTime created = appt.getCreatedAt() != null
                ? appt.getCreatedAt().atOffset(OffsetDateTime.now().getOffset())
                : OffsetDateTime.now();

        return AppointmentResponseDto.builder()
                .id(appt.getId())
                .userId(appt.getUser() != null ? appt.getUser().getTelegramId() : null)
                .masterId(appt.getMaster() != null ? appt.getMaster().getId() : null)
                .slotId(appt.getSlot() != null ? appt.getSlot().getId() : null)
                .services(services)
                .totalDuration(appt.getTotalDuration())
                .totalPrice(appt.getTotalPrice())
                .status(appt.getStatus())
                .createdAt(created)
                .build();
    }

    private ServiceSummaryDto toServiceSummary(MasterServiceWork s) {
        if (s == null) return null;
        Integer duration = null;
        Double price = null;
        if (s.getType() != null && s.getType().name().equals("FIXED") && s.getFixedDetails() != null) {
            duration = s.getFixedDetails().getDurationMinutes();
            price = s.getFixedDetails().getPrice();
        }
        return ServiceSummaryDto.builder()
                .id(s.getId())
                .name(s.getName())
                .type(s.getType() != null ? s.getType().name() : null)
                .durationMinutes(duration)
                .price(price)
                .build();
    }
}
