package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.DoctorNoteRequest;
import com.mediconnect.backend.dto.DoctorNoteResponse;
import com.mediconnect.backend.dto.ReviewRequest;
import com.mediconnect.backend.dto.ReviewResponse;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.model.DoctorNote;
import com.mediconnect.backend.model.Patient;
import com.mediconnect.backend.model.Review;
import com.mediconnect.backend.repository.DoctorNoteRepository;
import com.mediconnect.backend.repository.PatientRepository;
import com.mediconnect.backend.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorNoteAndReviewServiceTest {

    @Mock
    private DoctorNoteRepository doctorNoteRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private DoctorService doctorService;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private DoctorNoteService doctorNoteService;

    private ReviewService reviewService;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, doctorService, patientRepository);

        doctor = Doctor.builder()
                .doctorId(1L)
                .name("Dr. Banner")
                .specialization("Pediatrics")
                .build();

        patient = Patient.builder()
                .patientId(2L)
                .name("Peter Parker")
                .email("peter@example.com")
                .build();
    }

    @Test
    @DisplayName("Successfully create clinical doctor note")
    void testCreateDoctorNote_Success() {
        DoctorNoteRequest request = DoctorNoteRequest.builder()
                .doctorId(1L)
                .patientId(2L)
                .note("Patient exhibits mild allergic rhinitis. Prescribed antihistamines.")
                .build();

        when(doctorService.getDoctorEntity(1L)).thenReturn(doctor);
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

        DoctorNote saved = DoctorNote.builder()
                .noteId(10L)
                .doctor(doctor)
                .patient(patient)
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();
        when(doctorNoteRepository.save(any(DoctorNote.class))).thenReturn(saved);

        DoctorNoteResponse response = doctorNoteService.createDoctorNote(request);

        assertNotNull(response);
        assertEquals(10L, response.getNoteId());
        assertEquals("Dr. Banner", response.getDoctorName());
        assertEquals("Peter Parker", response.getPatientName());
        assertEquals(request.getNote(), response.getNote());
    }

    @Test
    @DisplayName("Successfully submit review and retrieve average rating")
    void testSubmitReviewAndAverageRating() {
        ReviewRequest request = ReviewRequest.builder()
                .patientId(2L)
                .doctorId(1L)
                .rating(5)
                .comment("Excellent doctor, very attentive and empathetic!")
                .build();

        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(doctorService.getDoctorEntity(1L)).thenReturn(doctor);

        Review saved = Review.builder()
                .reviewId(20L)
                .doctor(doctor)
                .patient(patient)
                .rating(5)
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewResponse response = reviewService.submitReview(request);

        assertNotNull(response);
        assertEquals(20L, response.getReviewId());
        assertEquals(5, response.getRating());
        assertEquals("Excellent doctor, very attentive and empathetic!", response.getComment());

        // Average rating calculation
        when(reviewRepository.getAverageRatingForDoctor(1L)).thenReturn(4.75);
        Double avgRating = reviewService.getAverageRatingForDoctor(1L);
        assertEquals(4.8, avgRating);
    }
}

