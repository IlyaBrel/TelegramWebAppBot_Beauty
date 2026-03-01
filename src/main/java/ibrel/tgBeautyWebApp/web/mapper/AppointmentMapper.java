package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentItemDto;
import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.booking.AppointmentServiceItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentMapper {

    public AppointmentResponseDto toDto(Appointment a, Integer totalDuration, Double totalPrice) {
        if (a == null) return null;

        List<AppointmentItemDto> items = a.getItems() == null ? List.of() :
                a.getItems().stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList());

        return AppointmentResponseDto.builder()
                .id(a.getId())
                .clientId(a.getClientId())
                .masterId(a.getMaster() != null ? a.getMaster().getId() : null)
                .slotId(a.getSlot() != null ? a.getSlot().getId() : null)
                .status(a.getStatus() != null ? a.getStatus().name() : null)
                .items(items)
                .totalDurationMinutes(totalDuration)
                .totalPrice(totalPrice)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    public AppointmentResponseDto toDto(Appointment a) {
        return toDto(a, null, null);
    }

    private AppointmentItemDto toItemDto(AppointmentServiceItem item) {
        return AppointmentItemDto.builder()
                .serviceId(item.getService() != null ? item.getService().getId() : null)
                .variableDetailIds(
                        item.getVariableDetails() == null ? List.of() :
                                item.getVariableDetails().stream()
                                        .map(v -> v.getId())
                                        .collect(Collectors.toList())
                )
                .build();
    }
}
