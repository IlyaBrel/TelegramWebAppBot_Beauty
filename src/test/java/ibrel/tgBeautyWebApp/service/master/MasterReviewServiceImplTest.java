package ibrel.tgBeautyWebApp.service.master;

import ibrel.tgBeautyWebApp.exception.EntityNotFoundException;
import ibrel.tgBeautyWebApp.model.booking.Appointment;
import ibrel.tgBeautyWebApp.model.master.Master;
import ibrel.tgBeautyWebApp.model.master.MasterReview;
import ibrel.tgBeautyWebApp.repository.AppointmentRepository;
import ibrel.tgBeautyWebApp.repository.MasterRepository;
import ibrel.tgBeautyWebApp.repository.MasterReviewRepository;
import ibrel.tgBeautyWebApp.service.master.impl.MasterReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterReviewServiceImplTest {

    @Mock MasterRepository masterRepository;
    @Mock MasterReviewRepository reviewRepository;
    @Mock AppointmentRepository appointmentRepository;

    @InjectMocks MasterReviewServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- ADD REVIEW ----------
    @Test
    void addReview_success_withoutAppointment() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(100L));
        review.setRating(5);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(reviewRepository.existsByMaster_IdAndAuthorId(masterId, String.valueOf(100L))).thenReturn(false);

        MasterReview saved = new MasterReview();
        saved.setId(10L);
        saved.setAuthorId(String.valueOf(100L));
        saved.setRating(5);

        when(reviewRepository.save(any())).thenReturn(saved);

        MasterReview result = service.addReview(masterId, review);

        assertEquals(10L, result.getId());
        verify(reviewRepository).save(any());
    }

    @Test
    void addReview_success_withAppointment() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        Appointment appointment = new Appointment();
        appointment.setId(50L);
        appointment.setMaster(master);

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(100L));
        review.setRating(4);
        review.setAppointment(appointment);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(appointmentRepository.existsById(50L)).thenReturn(true);
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(appointment));
        when(reviewRepository.existsByAppointment_Id(50L)).thenReturn(false);

        MasterReview saved = new MasterReview();
        saved.setId(20L);

        when(reviewRepository.save(any())).thenReturn(saved);

        MasterReview result = service.addReview(masterId, review);

        assertEquals(20L, result.getId());
    }

    @Test
    void addReview_whenMasterMissing_throwsNotFound() {
        when(masterRepository.findById(999L)).thenReturn(Optional.empty());

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(1L));
        review.setRating(5);

        assertThrows(EntityNotFoundException.class,
                () -> service.addReview(999L, review));
    }

    @Test
    void addReview_whenAppointmentMissing_throwsNotFound() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        Appointment appointment = new Appointment();
        appointment.setId(100L);

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(1L));
        review.setRating(5);
        review.setAppointment(appointment);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(appointmentRepository.existsById(100L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.addReview(masterId, review));
    }

    @Test
    void addReview_whenAppointmentNotBelongToMaster_throwsIllegalArgument() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        Master otherMaster = new Master();
        otherMaster.setId(2L);

        Appointment appointment = new Appointment();
        appointment.setId(100L);
        appointment.setMaster(otherMaster);

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(1L));
        review.setRating(5);
        review.setAppointment(appointment);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(appointmentRepository.existsById(100L)).thenReturn(true);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> service.addReview(masterId, review));
    }

    @Test
    void addReview_whenDuplicateReviewForAppointment_throwsIllegalState() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        Appointment appointment = new Appointment();
        appointment.setId(100L);
        appointment.setMaster(master);

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(1L));
        review.setRating(5);
        review.setAppointment(appointment);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(appointmentRepository.existsById(100L)).thenReturn(true);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(reviewRepository.existsByAppointment_Id(100L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.addReview(masterId, review));
    }

    @Test
    void addReview_whenDuplicateAuthorReview_throwsIllegalState() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(100L));
        review.setRating(5);

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(reviewRepository.existsByMaster_IdAndAuthorId(masterId, String.valueOf(100L))).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.addReview(masterId, review));
    }

    @Test
    void addReview_whenRatingInvalid_throwsIllegalArgument() {
        Long masterId = 1L;

        Master master = new Master();
        master.setId(masterId);

        MasterReview review = new MasterReview();
        review.setAuthorId(String.valueOf(1L));
        review.setRating(10); // invalid

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));

        assertThrows(IllegalArgumentException.class,
                () -> service.addReview(masterId, review));
    }

    // ---------- GET ----------
    @Test
    void getById_success() {
        MasterReview review = new MasterReview();
        review.setId(5L);

        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));

        MasterReview result = service.getById(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void getById_notFound() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getById(5L));
    }

    @Test
    void getByMaster_success() {
        Long masterId = 1L;

        when(masterRepository.existsById(masterId)).thenReturn(true);

        List<MasterReview> list = List.of(new MasterReview());
        when(reviewRepository.findByMasterIdOrderByCreatedAtDesc(masterId)).thenReturn(list);

        List<MasterReview> result = service.getByMaster(masterId);

        assertEquals(1, result.size());
    }

    @Test
    void getByMaster_notFound() {
        when(masterRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.getByMaster(1L));
    }

    // ---------- DELETE ----------
    @Test
    void delete_success() {
        when(reviewRepository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(reviewRepository).deleteById(10L);
    }

    @Test
    void delete_notFound() {
        when(reviewRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(10L));
    }

    // ---------- AVERAGE RATING ----------
    @Test
    void getAverageRating_success() {
        when(masterRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findAverageRatingByMasterId(1L)).thenReturn(4.567);

        double avg = service.getAverageRating(1L);

        assertEquals(4.57, avg);
    }

    @Test
    void getAverageRating_noReviews_returnsZero() {
        when(masterRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findAverageRatingByMasterId(1L)).thenReturn(null);

        double avg = service.getAverageRating(1L);

        assertEquals(0.0, avg);
    }

    @Test
    void getAverageRating_masterNotFound() {
        when(masterRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> service.getAverageRating(1L));
    }
}
