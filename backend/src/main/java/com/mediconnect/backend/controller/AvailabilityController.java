package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.AvailabilityRequest;
import com.mediconnect.backend.dto.AvailabilityResponse;
import com.mediconnect.backend.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
@Tag(name = "Doctor Availability", description = "Endpoints for configuring and querying doctor availability schedules")
@SecurityRequirement(name = "BearerAuth")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Operation(summary = "Create working availability slot", description = "Defines a doctor's availability window on a specific date.")
    @PostMapping
    public ResponseEntity<AvailabilityResponse> createAvailability(@Valid @RequestBody AvailabilityRequest request) {
        AvailabilityResponse response = availabilityService.createAvailability(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get doctor availability slots", description = "Retrieves all availability windows for a doctor, optionally filtered by date.")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AvailabilityResponse>> getDoctorAvailabilities(
            @PathVariable Long doctorId,
            @Parameter(description = "Optional filter by date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AvailabilityResponse> responses;
        if (date != null) {
            responses = availabilityService.getAvailabilitiesByDoctorAndDate(doctorId, date);
        } else {
            responses = availabilityService.getAvailabilitiesByDoctor(doctorId);
        }
        return ResponseEntity.ok(responses);
    }
}

