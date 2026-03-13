package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.MasterReviewDto;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import ibrel.tgBeautyWebApp.service.master.MasterReviewService;
import ibrel.tgBeautyWebApp.service.master.MasterService;
import ibrel.tgBeautyWebApp.web.mapper.master.MasterReviewMapper;
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
    private final MasterService       masterService;
    private final MasterReviewMapper  mapper;

    // ── Чтение ────────────────────────────────────────────────────────────────

    @GetMapping("/masters/{masterId}/reviews")
    public ResponseEntity<List<MasterReviewDto>> getReviews(@PathVariable Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        List<MasterReviewDto> list = reviewService.getByMaster(masterId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/masters/{masterId}/reviews/average")
    public ResponseEntity<Double> getAverage(@PathVariable Long masterId) {
        double avg = reviewService.getAverageRating(masterId);
        return ResponseEntity.ok(avg);
    }

    // ── Создание ──────────────────────────────────────────────────────────────

    /**
     * Реальный отзыв клиента (привязан к записи).
     * Валидация: rating @NotNull, @Min(1), @Max(5); authorId @NotNull.
     */
    @PostMapping("/masters/{masterId}/reviews")
    public ResponseEntity<MasterReviewDto> addReview(@PathVariable Long masterId,
                                                     @Valid @RequestBody MasterReviewDto dto) {
        MasterReview entity = mapper.toEntity(dto);
        MasterReview created = reviewService.addReview(masterId, entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    /**
     * Ручной отзыв — только для admin.
     * Проверка роли и установка isManual=true делегируется в MasterService.addManualReview().
     */
    @PostMapping("/masters/{masterId}/reviews/manual")
    public ResponseEntity<MasterReviewDto> addManualReview(
            @PathVariable Long masterId,
            @RequestBody MasterReviewDto dto,
            @RequestParam Long adminTelegramId) {

        MasterReview created = masterService.addManualReview(
                masterId, mapper.toEntity(dto), adminTelegramId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    // ── Удаление ──────────────────────────────────────────────────────────────

    /**
     * Удалить отзыв — только admin.
     * Проверка роли выполняется в MasterService.deleteReview(),
     * который вызывает requireAdmin() перед делегированием в reviewService.delete(Long).
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @RequestParam Long adminTelegramId) {
        masterService.deleteReview(reviewId, adminTelegramId);
        return ResponseEntity.noContent().build();
    }
}