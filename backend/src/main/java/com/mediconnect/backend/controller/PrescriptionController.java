package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.PrescriptionRequest;
import com.mediconnect.backend.dto.PrescriptionResponse;
import com.mediconnect.backend.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Endpoints for generating and viewing digital prescriptions")
@SecurityRequirement(name = "BearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @Operation(summary = "Issue digital prescription", description = "Generates and issues a digital prescription for a recorded consultation.")
    @PostMapping
    public ResponseEntity<PrescriptionResponse> issuePrescription(@Valid @RequestBody PrescriptionRequest request) {
        PrescriptionResponse response = prescriptionService.issuePrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get prescriptions for a patient", description = "Retrieves all issued prescriptions for a given patient ID.")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByPatient(@PathVariable Long patientId) {
        List<PrescriptionResponse> responses = prescriptionService.getPrescriptionsByPatientId(patientId);
        return ResponseEntity.ok(responses);
    }
}

