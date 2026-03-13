package ibrel.tgBeautyWebApp.web.controller.catalog;

import ibrel.tgBeautyWebApp.dto.master.MasterDto;
import ibrel.tgBeautyWebApp.service.catalog.FavoriteMasterService;
import ibrel.tgBeautyWebApp.web.mapper.master.MasterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Избранные мастера пользователя.
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteMasterController {

    private final FavoriteMasterService favService;
    private final MasterMapper          masterMapper;

    /** Мои избранные мастера */
    @GetMapping
    public ResponseEntity<List<MasterDto>> getFavorites(@RequestParam Long telegramId) {
        return ResponseEntity.ok(
                favService.getFavorites(telegramId)
                        .stream().map(masterMapper::toDto).toList());
    }

    /** Добавить мастера в избранное */
    @PostMapping("/{masterId}")
    public ResponseEntity<Void> add(@PathVariable Long masterId,
                                    @RequestParam Long telegramId) {
        favService.add(telegramId, masterId);
        return ResponseEntity.ok().build();
    }

    /** Убрать мастера из избранного */
    @DeleteMapping("/{masterId}")
    public ResponseEntity<Void> remove(@PathVariable Long masterId,
                                       @RequestParam Long telegramId) {
        favService.remove(telegramId, masterId);
        return ResponseEntity.noContent().build();
    }

    /** Проверить — мастер в избранном? */
    @GetMapping("/{masterId}/check")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long masterId,
                                              @RequestParam Long telegramId) {
        return ResponseEntity.ok(favService.isFavorite(telegramId, masterId));
    }
}