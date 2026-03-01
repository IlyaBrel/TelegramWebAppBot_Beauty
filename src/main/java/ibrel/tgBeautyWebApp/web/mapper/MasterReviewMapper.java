package ibrel.tgBeautyWebApp.web.mapper;

import ibrel.tgBeautyWebApp.dto.master.MasterReviewDto;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import org.springframework.stereotype.Component;

@Component
public class MasterReviewMapper {

    public MasterReviewDto toDto(MasterReview r) {
        if (r == null) return null;
        return MasterReviewDto.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .authorId(r.getAuthorId())
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
        r.setAuthorId(dto.getAuthorId());
        // createdAt устанавливается в сервисе
        if (dto.getAppointmentId() != null) {
            Appointment a = new Appointment();
            a.setId(dto.getAppointmentId());
            r.setAppointment(a);
        }
        return r;
    }
}
