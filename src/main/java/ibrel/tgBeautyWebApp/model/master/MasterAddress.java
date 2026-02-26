package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;       // город
    private String street;     // улица
    private String house;      // дом
    private String floor;      // этаж
    private String apartment;  // квартира

    private String placeOnTheMap; //карта

    @OneToOne
    @JoinColumn(name = "master_id")
    private Master master;     // связь с мастером
}
