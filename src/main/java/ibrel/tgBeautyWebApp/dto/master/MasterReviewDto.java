package ibrel.tgBeautyWebApp.dto.master;

import lombok.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterReviewDto {
    private Long id;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be >= 1")
    @Max(value = 5, message = "rating must be <= 5")
    private Integer rating;

    @Size(max = 2000, message = "comment length must be <= 2000")
    private String comment;

    @NotNull(message = "authorId is required")
    private String authorId;

    private OffsetDateTime createdAt;

    private Long appointmentId;
}
