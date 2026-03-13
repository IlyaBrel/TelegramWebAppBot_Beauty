package ibrel.tgBeautyWebApp.web.mapper.appointment;

import ibrel.tgBeautyWebApp.dto.booking.AppointmentItemDto;
import ibrel.tgBeautyWebApp.dto.booking.AppointmentResponseDto;
import ibrel.tgBeautyWebApp.dto.booking.WorkSlotShortDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.booking.AppointmentServiceItem;
import ibrel.tgBeautyWebApp.model.booking.AppointmentSlot;
import ibrel.tgBeautyWebApp.web.mapper.master.WorkSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AppointmentMapper {

    private final WorkSlotMapper workSlotMapper;

    public AppointmentResponseDto toDto(Appointment a, Integer totalDuration, Double totalPrice) {
        if (a == null) return null;

        // Слоты — сортируем по slotOrder
        List<WorkSlotShortDto> slots = a.getSlots() == null ? List.of() :
                a.getSlots().stream()
                        .sorted(Comparator.comparingInt(AppointmentSlot::getSlotOrder))
                        .map(as -> workSlotMapper.toShortDto(as.getSlot()))
                        .collect(Collectors.toList());

        // Услуги
        List<AppointmentItemDto> items = a.getItems() == null ? List.of() :
                a.getItems().stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList());

        // Имя мастера для удобства
        String masterName = null;
        if (a.getMaster() != null && a.getMaster().getPersonalData() != null) {
            masterName = a.getMaster().getPersonalData().getFirstName();
        }

        // Код промокода
        String promoCode = a.getPromoCode() != null ? a.getPromoCode().getCode() : null;

        return AppointmentResponseDto.builder()
                .id(a.getId())
                .clientTelegramId(a.getClientTelegramId())
                .masterId(a.getMaster() != null ? a.getMaster().getId() : null)
                .masterName(masterName)
                .slots(slots)
                .status(a.getStatus() != null ? a.getStatus().name() : null)
                .rejectionReason(a.getRejectionReason())
                .items(items)
                .totalDurationMinutes(totalDuration)
                .totalPrice(totalPrice)
                .promoCode(promoCode)
                .promoDiscountAmount(a.getPromoDiscountAmount())
                .bonusAmountUsed(a.getBonusAmountUsed())
                .cashbackAmount(a.getCashbackAmount())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    public AppointmentResponseDto toDto(Appointment a) {
        return toDto(a, a.getTotalDurationMinutes(), a.getTotalPrice());
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
