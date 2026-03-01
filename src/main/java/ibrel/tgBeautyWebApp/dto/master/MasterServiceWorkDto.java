package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterServiceWorkDto {
    private Long id;
    private Long masterId;
    private String name;
    private String type; // FIXED or VARIABLE
    private String description;
    private FixedServiceDetailsDto fixedDetails;
    private List<VariableServiceDetailsDto> variableDetails;
}
