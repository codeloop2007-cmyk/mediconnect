package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.ReviewRequest;
import com.mediconnect.backend.dto.ReviewResponse;
import com.mediconnect.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Endpoints for submitting and viewing post-consultation doctor reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Submit a doctor review",
            description = "Submits a rating (1-5) and comment after a consultation."
    )
    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.submitReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get reviews for a doctor", description = "Retrieves all patient ratings and reviews for a doctor.")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ReviewResponse>> getDoctorReviews(@PathVariable Long doctorId) {
        List<ReviewResponse> responses = reviewService.getReviewsByDoctor(doctorId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get average rating for a doctor", description = "Retrieves the aggregated average review rating for a doctor.")
    @GetMapping("/doctor/{doctorId}/average")
    public ResponseEntity<Map<String, Object>> getDoctorAverageRating(@PathVariable Long doctorId) {
        Double avgRating = reviewService.getAverageRatingForDoctor(doctorId);
        return ResponseEntity.ok(Map.of(
                "doctorId", doctorId,
                "averageRating", avgRating
        ));
    }
}

