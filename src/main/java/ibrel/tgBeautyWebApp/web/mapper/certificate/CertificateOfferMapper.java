package ibrel.tgBeautyWebApp.web.mapper.certificate;

import ibrel.tgBeautyWebApp.dto.certificate.CertificateOfferDto;
import ibrel.tgBeautyWebApp.model.certificate.CertificateOffer;
import ibrel.tgBeautyWebApp.model.master.service.MasterServiceWork;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CertificateOfferMapper {

    public CertificateOfferDto toDto(CertificateOffer o) {
        if (o == null) return null;

        String masterName = null;
        if (o.getMaster() != null && o.getMaster().getPersonalData() != null) {
            masterName = o.getMaster().getPersonalData().getFirstName();
        }

        List<Long> serviceIds = o.getServices() == null ? List.of() :
                o.getServices().stream().map(MasterServiceWork::getId).collect(Collectors.toList());

        List<String> serviceNames = o.getServices() == null ? List.of() :
                o.getServices().stream().map(MasterServiceWork::getName).collect(Collectors.toList());

        return CertificateOfferDto.builder()
                .id(o.getId())
                .masterId(o.getMaster() != null ? o.getMaster().getId() : null)
                .masterName(masterName)
                .title(o.getTitle())
                .description(o.getDescription())
                .serviceIds(serviceIds)
                .serviceNames(serviceNames)
                .price(o.getPrice())
                .validDays(o.getValidDays())
                .active(o.getActive())
                .createdAt(o.getCreatedAt())
                .build();
    }

    /** toEntity — только поля без связей (master и services проставляет сервис) */
    public CertificateOffer toEntity(CertificateOfferDto dto) {
        if (dto == null) return null;
        return CertificateOffer.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .validDays(dto.getValidDays())
                .build();
    }
}
