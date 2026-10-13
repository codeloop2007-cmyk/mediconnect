package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.DoctorRequest;
import com.mediconnect.backend.dto.DoctorResponse;
import com.mediconnect.backend.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@Tag(name = "Doctors", description = "Endpoints for searching and managing doctor profiles")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @Operation(
            summary = "Semantic search doctors by symptoms",
            description = "Performs PostgreSQL pgvector cosine similarity search over doctor embeddings based on patient symptoms."
    )
    @GetMapping("/semantic-search")
    public ResponseEntity<List<DoctorResponse>> semanticSearch(
            @Parameter(description = "Patient symptoms or medical inquiry", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of doctor matches to return (default 5)")
            @RequestParam(defaultValue = "5") int limit) {
        List<DoctorResponse> doctors = doctorService.semanticSearchDoctors(query, limit);
        return ResponseEntity.ok(doctors);
    }

    @Operation(
            summary = "Search and filter doctors",
            description = "Search doctors by specialization, language, location, and maximum consultation fee. Includes average ratings."
    )
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> searchDoctors(
            @Parameter(description = "Doctor specialization e.g. Cardiology, Dermatology")
            @RequestParam(required = false) String specialization,
            @Parameter(description = "Spoken language e.g. English, Spanish")
            @RequestParam(required = false) String language,
            @Parameter(description = "Practice location or clinic")
            @RequestParam(required = false) String location,
            @Parameter(description = "Maximum consultation fee")
            @RequestParam(required = false) BigDecimal maxFee) {

        List<DoctorResponse> doctors = doctorService.searchDoctors(specialization, language, location, maxFee);
        return ResponseEntity.ok(doctors);
    }

    @Operation(summary = "Get doctor details by ID", description = "Retrieves doctor details including average rating and reviews count.")
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create a doctor profile",
            description = "Creates a new doctor profile in the system and automatically generates vector embedding in PostgreSQL via pgvector."
    )
    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

