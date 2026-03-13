package ibrel.tgBeautyWebApp.service.scheduler;

import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.booking.AppointmentSlot;
import ibrel.tgBeautyWebApp.model.master.WorkSlot;
import ibrel.tgBeautyWebApp.repository.appointment.AppointmentRepository;
import ibrel.tgBeautyWebApp.service.tg.TelegramNotificationService;
import ibrel.tgBeautyWebApp.service.booking.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Планировщик задач:
 * - Напоминания пользователям за 24 часа до записи
 * - Запросы на отзыв после завершения записи
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final AppointmentRepository      appointmentRepository;
    private final AppointmentService         appointmentService;
    private final TelegramNotificationService telegramService;

    /** Напоминания — каждый час */
    @Scheduled(fixedRate = 3_600_000)
    public void sendReminders() {
        List<Appointment> appointments = appointmentRepository.findConfirmedWithoutReminder();
        DayOfWeek tomorrow = LocalDate.now().plusDays(1).getDayOfWeek();

        for (Appointment a : appointments) {
            try {
                WorkSlot firstSlot = getFirstSlot(a);
                if (firstSlot == null) continue;

                // Проверяем что запись завтра
                if (!DayOfWeek.valueOf(firstSlot.getDayOfWeek()).equals(tomorrow)) continue;

                String masterName = a.getMaster().getPersonalData() != null
                        ? a.getMaster().getPersonalData().getFirstName() : "Мастер";

                telegramService.notifyReminder(
                        a.getClientTelegramId(),
                        masterName,
                        firstSlot.getDayOfWeek(),
                        firstSlot.getStartTime().toString(),
                        24);

                a.setReminderSent(true);
                appointmentRepository.save(a);
                log.info("Sent reminder for appointment id={}", a.getId());

            } catch (Exception e) {
                log.error("Failed to send reminder for appointment id={}: {}", a.getId(), e.getMessage());
            }
        }
    }

    /** Запросы на отзыв — каждые 30 минут */
    @Scheduled(fixedRate = 1_800_000)
    public void sendReviewRequests() {
        List<Appointment> completed = appointmentRepository
                .findByStatusAndReviewRequestSentFalse(Appointment.Status.COMPLETED);

        for (Appointment a : completed) {
            try {
                appointmentService.sendReviewRequest(a.getId());
                log.info("Sent review request for appointment id={}", a.getId());
            } catch (Exception e) {
                log.error("Failed to send review request for appointment id={}: {}", a.getId(), e.getMessage());
            }
        }
    }

    private WorkSlot getFirstSlot(Appointment a) {
        if (a.getSlots() == null || a.getSlots().isEmpty()) return null;
        AppointmentSlot first = a.getSlots().stream()
                .filter(s -> s.getSlotOrder() == 0)
                .findFirst().orElse(null);
        return first != null ? first.getSlot() : null;
    }
}