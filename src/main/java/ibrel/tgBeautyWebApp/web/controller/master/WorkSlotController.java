package ibrel.tgBeautyWebApp.web.controller.master;

import ibrel.tgBeautyWebApp.dto.master.WorkSlotDto;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.service.master.WorkSlotService;
import ibrel.tgBeautyWebApp.web.mapper.WorkSlotMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class WorkSlotController {

    private final WorkSlotService slotService;
    private final WorkSlotMapper mapper;

    /**
     * Получить все слоты мастера.
     * GET /api/masters/{masterId}/slots
     */
    @GetMapping("/{masterId}/slots")
    public ResponseEntity<List<WorkSlotDto>> getSlots(@PathVariable Long masterId) {
        Assert.notNull(masterId, "masterId must not be null");
        List<WorkSlotDto> list = slotService.findByMaster(masterId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Создать слот для мастера.
     * POST /api/masters/{masterId}/slots
     */
    @PostMapping("/{masterId}/slots")
    public ResponseEntity<WorkSlotDto> createSlot(@PathVariable Long masterId,
                                                  @Valid @RequestBody WorkSlotDto dto) {
        Assert.notNull(dto, "WorkSlotDto must not be null");
        WorkSlot entity = mapper.toEntity(dto);
        WorkSlot created = slotService.create(masterId, entity);
        return ResponseEntity.ok(mapper.toDto(created));
    }

    /**
     * Обновить слот.
     * PUT /api/masters/slots/{slotId}
     */
    @PutMapping("/slots/{slotId}")
    public ResponseEntity<WorkSlotDto> updateSlot(@PathVariable Long slotId,
                                                  @Valid @RequestBody WorkSlotDto dto) {
        WorkSlot entity = mapper.toEntity(dto);
        WorkSlot updated = slotService.update(slotId, entity);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    /**
     * Удалить слот (сервис проверит наличие записей и принадлежность).
     * DELETE /api/masters/{masterId}/slots/{slotId}
     */
    @DeleteMapping("/{masterId}/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long masterId, @PathVariable Long slotId) {
        slotService.delete(slotId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить доступные слоты мастера на конкретную дату (по дню недели).
     * GET /api/masters/{masterId}/slots/available?date=YYYY-MM-DD
     */
    @GetMapping("/{masterId}/slots/available")
    public ResponseEntity<List<WorkSlotDto>> findAvailable(
            @PathVariable Long masterId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Assert.notNull(date, "date must not be null");
        List<WorkSlotDto> list = slotService.findAvailable(masterId, date).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
