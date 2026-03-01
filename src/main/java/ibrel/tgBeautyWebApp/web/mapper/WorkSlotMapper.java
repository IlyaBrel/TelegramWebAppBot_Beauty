package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.WorkSlotDto;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import org.springframework.stereotype.Component;

@Component
public class WorkSlotMapper {

    public WorkSlotDto toDto(WorkSlot s) {
        if (s == null) return null;
        return WorkSlotDto.builder()
                .id(s.getId())
                .masterId(s.getMaster() != null ? s.getMaster().getId() : null)
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .note(s.getNote())
                .build();
    }

    public WorkSlot toEntity(WorkSlotDto dto) {
        if (dto == null) return null;
        WorkSlot s = new WorkSlot();
        s.setId(dto.getId());
        s.setDayOfWeek(dto.getDayOfWeek());
        s.setStartTime(dto.getStartTime());
        s.setEndTime(dto.getEndTime());
        s.setNote(dto.getNote());
        return s;
    }
}
