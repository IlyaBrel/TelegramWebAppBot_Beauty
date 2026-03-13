package ibrel.tgBeautyWebApp.dto.certificate;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDto {
    private Long   id;
    private String code;
    private String status;          // ACTIVE, USED, EXPIRED, CANCELLED

    private Long   masterId;
    private String masterName;

    private Long   recipientTelegramId;

    /** Услуги, входящие в сертификат */
    private List<ServiceShortDto> services;

    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;

    /** ID записи, в которой использован сертификат */
    private Long usedInAppointmentId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceShortDto {
        private Long   id;
        private String name;
    }
}
