package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.ReviewRequest;
import com.mediconnect.backend.dto.ReviewResponse;
import com.mediconnect.backend.exception.ResourceNotFoundException;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.model.Patient;
import com.mediconnect.backend.model.Review;
import com.mediconnect.backend.repository.PatientRepository;
import com.mediconnect.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DoctorService doctorService;
    private final PatientRepository patientRepository;

    @Transactional
    public ReviewResponse submitReview(ReviewRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));
        Doctor doctor = doctorService.getDoctorEntity(request.getDoctorId());

        Review review = Review.builder()
                .patient(patient)
                .doctor(doctor)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review submitted successfully with ID: {}", saved.getReviewId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByDoctor(Long doctorId) {
        doctorService.getDoctorEntity(doctorId);

        return reviewRepository.findByDoctorDoctorId(doctorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingForDoctor(Long doctorId) {
        doctorService.getDoctorEntity(doctorId);
        Double avg = reviewRepository.getAverageRatingForDoctor(doctorId);
        return avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .patientId(review.getPatient().getPatientId())
                .patientName(review.getPatient().getName())
                .doctorId(review.getDoctor().getDoctorId())
                .doctorName(review.getDoctor().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

