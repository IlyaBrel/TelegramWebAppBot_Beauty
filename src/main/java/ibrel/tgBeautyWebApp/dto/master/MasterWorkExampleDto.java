package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterWorkExampleDto {
    private Long id;

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title length must be <= 200")
    private String title;

    @Size(max = 2000, message = "description length must be <= 2000")
    private String description;

    @Size(max = 1000, message = "imageUrl length must be <= 1000")
    private String imageUrl;

    private OffsetDateTime createdAt;
}
