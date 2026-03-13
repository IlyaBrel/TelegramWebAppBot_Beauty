package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDto {
    private Long    id;
    private Long    telegramId;
    private Boolean active;
    private String  imageUrl;

    /**
     * Название профиля мастера — для различения нескольких аккаунтов.
     * Например: «Маникюр», «Брови и ресницы».
     */
    private String profileName;

    private MasterPersonalDataDto personalData;
    private MasterAddressDto      address;

    private String cityName;
    private Long   cityId;

    private List<String> specialties;
    private List<String> amenities;

    private Double  cachedAvgRating;
    private Double  boostedRating;
    private Double  displayRating;
    private Integer reviewCount;
    private Double  averageRating;
}