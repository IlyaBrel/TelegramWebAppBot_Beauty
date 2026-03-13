package ibrel.tgBeautyWebApp.web.mapper.certificate;

import ibrel.tgBeautyWebApp.dto.certificate.CertificateDto;
import ibrel.tgBeautyWebApp.model.certificate.Certificate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CertificateMapper {

    public CertificateDto toDto(Certificate c) {
        if (c == null) return null;

        String masterName = null;
        if (c.getMaster() != null && c.getMaster().getPersonalData() != null) {
            masterName = c.getMaster().getPersonalData().getFirstName();
        }

        List<CertificateDto.ServiceShortDto> services = c.getServices() == null ? List.of() :
                c.getServices().stream()
                        .map(s -> CertificateDto.ServiceShortDto.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .build())
                        .collect(Collectors.toList());

        return CertificateDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .masterId(c.getMaster() != null ? c.getMaster().getId() : null)
                .masterName(masterName)
                .recipientTelegramId(c.getRecipientTelegramId())
                .services(services)
                .createdAt(c.getCreatedAt())
                .expiresAt(c.getExpiresAt())
                .usedAt(c.getUsedAt())
                .usedInAppointmentId(c.getUsedInAppointmentId())
                .build();
    }
}
