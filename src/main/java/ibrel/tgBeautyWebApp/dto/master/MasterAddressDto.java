package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterAddressDto {
    private Long   id;
    private String city;
    private String street;
    private String house;
    private String floor;
    private String apartment;
    private String placeOnTheMap;

    /** Широта — для отображения на карте */
    private Double latitude;

    /** Долгота — для отображения на карте */
    private Double longitude;
}