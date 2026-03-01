package ibrel.tgBeautyWebApp.model.master;

import jakarta.persistence.*;
import lombok.*;

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

    private String city;
    private String street;
    private String house;
    private String floor;
    private String apartment;

    @Column(name = "place_on_the_map", length = 500)
    private String placeOnTheMap;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", unique = true)
    private Master master;
}
