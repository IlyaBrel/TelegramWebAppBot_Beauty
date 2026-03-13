package ibrel.tgBeautyWebApp.web.mapper.master;

import ibrel.tgBeautyWebApp.dto.master.MasterReviewDto;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import org.springframework.stereotype.Component;

@Component
public class MasterReviewMapper {

    public MasterReviewDto toDto(MasterReview r) {
        if (r == null) return null;

        // authorId в модели хранится как String (telegramId.toString())
        Long authorTelegramId = null;
        if (r.getAuthorId() != null) {
            try { authorTelegramId = Long.parseLong(r.getAuthorId()); }
            catch (NumberFormatException ignored) {}
        }

        return MasterReviewDto.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .authorTelegramId(authorTelegramId)
                .isManual(r.getIsManual())
                .createdAt(r.getCreatedAt())
                .appointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null)
                .build();
    }

    public MasterReview toEntity(MasterReviewDto dto) {
        if (dto == null) return null;
        MasterReview r = new MasterReview();
        r.setId(dto.getId());
        r.setRating(dto.getRating());
        r.setComment(dto.getComment());
        // authorId в модели String — конвертируем из Long
        r.setAuthorId(dto.getAuthorTelegramId() != null
                ? dto.getAuthorTelegramId().toString() : null);
        r.setIsManual(Boolean.TRUE.equals(dto.getIsManual()));
        if (dto.getAppointmentId() != null) {
            Appointment a = new Appointment();
            a.setId(dto.getAppointmentId());
            r.setAppointment(a);
        }
        return r;
    }
}