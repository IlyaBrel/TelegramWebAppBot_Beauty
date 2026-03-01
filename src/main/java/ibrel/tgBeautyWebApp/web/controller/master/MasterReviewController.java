package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.MasterReviewDto;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import ibrel.tgBeautyWebApp.service.master.MasterReviewService;
import ibrel.tgBeautyWebApp.web.mapper.MasterReviewMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MasterReviewController {

    private final MasterReviewService reviewService;
    private final MasterReviewMapper mapper;

    @GetMapping("/masters/{masterId}/reviews")
    public ResponseEntity<List<MasterReviewDto>> getReviews(@PathVariable Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        List<MasterReviewDto> list = reviewService.getByMaster(masterId).stream().map(mapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/masters/{masterId}/reviews")
    public ResponseEntity<MasterReviewDto> addReview(@PathVariable Long masterId, @Valid @RequestBody MasterReviewDto dto) {
        MasterReview entity = mapper.toEntity(dto);
        MasterReview created = reviewService.addReview(masterId, entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @GetMapping("/masters/{masterId}/reviews/average")
    public ResponseEntity<Double> getAverage(@PathVariable Long masterId) {
        double avg = reviewService.getAverageRating(masterId);
        return ResponseEntity.ok(avg);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.delete(reviewId);
        return ResponseEntity.noContent().build();
    }
}
