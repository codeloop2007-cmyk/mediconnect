package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.DoctorResponse;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.model.Review;
import com.mediconnect.backend.repository.DoctorRepository;
import com.mediconnect.backend.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder()
                .doctorId(1L)
                .name("Dr. House")
                .specialization("Diagnostics")
                .qualification("MD")
                .language("English")
                .location("Princeton")
                .consultationFee(BigDecimal.valueOf(200.00))
                .build();
    }

    @Test
    @DisplayName("Search doctors returns matching doctors with ratings")
    void testSearchDoctors() {
        when(doctorRepository.searchDoctors(eq("diagnostics"), eq("english"), isNull(), isNull()))
                .thenReturn(List.of(doctor));
        when(reviewRepository.getAverageRatingForDoctor(1L)).thenReturn(4.8);
        when(reviewRepository.findByDoctorDoctorId(1L)).thenReturn(List.of(new Review(), new Review()));

        List<DoctorResponse> results = doctorService.searchDoctors("Diagnostics", "English", null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Dr. House", results.get(0).getName());
        assertEquals(4.8, results.get(0).getAverageRating());
        assertEquals(2, results.get(0).getTotalReviews());
    }

    @Test
    @DisplayName("Get doctor by ID returns details and rating")
    void testGetDoctorById() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(reviewRepository.getAverageRatingForDoctor(1L)).thenReturn(5.0);
        when(reviewRepository.findByDoctorDoctorId(1L)).thenReturn(List.of(new Review()));

        DoctorResponse result = doctorService.getDoctorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getDoctorId());
        assertEquals("Dr. House", result.getName());
        assertEquals(5.0, result.getAverageRating());
        assertEquals(1, result.getTotalReviews());
    }
}

