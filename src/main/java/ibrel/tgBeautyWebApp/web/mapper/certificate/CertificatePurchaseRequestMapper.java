package ibrel.tgBeautyWebApp.web.mapper.certificate;

import ibrel.tgBeautyWebApp.dto.certificate.CertificatePurchaseRequestDto;
import ibrel.tgBeautyWebApp.model.certificate.CertificatePurchaseRequest;
import org.springframework.stereotype.Component;

@Component
public class CertificatePurchaseRequestMapper {

    public CertificatePurchaseRequestDto toDto(CertificatePurchaseRequest r) {
        if (r == null) return null;

        String certCode = r.getCertificate() != null ? r.getCertificate().getCode() : null;

        return CertificatePurchaseRequestDto.builder()
                .id(r.getId())
                .offerId(r.getOffer() != null ? r.getOffer().getId() : null)
                .offerTitle(r.getOffer() != null ? r.getOffer().getTitle() : null)
                .offerPrice(r.getOffer() != null ? r.getOffer().getPrice() : null)
                .buyerTelegramId(r.getBuyerTelegramId())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .buyerComment(r.getBuyerComment())
                .rejectionReason(r.getRejectionReason())
                .certificateCode(certCode)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
