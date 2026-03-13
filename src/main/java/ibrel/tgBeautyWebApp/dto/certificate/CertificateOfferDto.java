package ibrel.tgBeautyWebApp.dto.certificate;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateOfferDto {
    private Long id;

    private Long   masterId;
    private String masterName;

    @NotBlank(message = "title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    /** ID услуг, входящих в оффер */
    @NotEmpty(message = "services must not be empty")
    private List<Long> serviceIds;

    /** Краткие названия услуг (для отображения) */
    private List<String> serviceNames;

    @NotNull(message = "price is required")
    @Positive(message = "price must be > 0")
    private Double price;

    /** Срок действия в днях (null = бессрочно) */
    private Integer validDays;

    private Boolean active;

    private OffsetDateTime createdAt;
}
