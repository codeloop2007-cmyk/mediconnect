package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.ConsultationRequest;
import com.mediconnect.backend.dto.ConsultationResponse;
import com.mediconnect.backend.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
@Tag(name = "Consultations", description = "Endpoints for recording and accessing completed consultation sessions")
@SecurityRequirement(name = "BearerAuth")
public class ConsultationController {

    private final ConsultationService consultationService;

    @Operation(summary = "Record a consultation", description = "Records a consultation for a completed appointment.")
    @PostMapping
    public ResponseEntity<ConsultationResponse> createConsultation(@Valid @RequestBody ConsultationRequest request) {
        ConsultationResponse response = consultationService.createConsultation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get consultation by appointment ID", description = "Retrieves consultation details for an appointment.")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ConsultationResponse> getConsultationByAppointmentId(@PathVariable Long appointmentId) {
        ConsultationResponse response = consultationService.getConsultationByAppointmentId(appointmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get consultation by ID", description = "Retrieves consultation details by consultation ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> getConsultationById(@PathVariable Long id) {
        ConsultationResponse response = consultationService.getConsultationById(id);
        return ResponseEntity.ok(response);
    }
}

