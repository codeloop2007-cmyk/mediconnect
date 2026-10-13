package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.DoctorNoteRequest;
import com.mediconnect.backend.dto.DoctorNoteResponse;
import com.mediconnect.backend.service.DoctorNoteService;
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
@RequestMapping("/api/v1/doctor-notes")
@RequiredArgsConstructor
@Tag(name = "Doctor Notes", description = "Endpoints for managing private clinical notes on patient history")
@SecurityRequirement(name = "BearerAuth")
public class DoctorNoteController {

    private final DoctorNoteService doctorNoteService;

    @Operation(summary = "Add clinical doctor note", description = "Adds a private clinical note for patient history tracking.")
    @PostMapping
    public ResponseEntity<DoctorNoteResponse> createDoctorNote(@Valid @RequestBody DoctorNoteRequest request) {
        DoctorNoteResponse response = doctorNoteService.createDoctorNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get notes for a patient", description = "Retrieves all clinical notes recorded for a patient.")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DoctorNoteResponse>> getDoctorNotesByPatient(@PathVariable Long patientId) {
        List<DoctorNoteResponse> responses = doctorNoteService.getDoctorNotesByPatient(patientId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get notes by doctor and patient", description = "Retrieves notes written by a specific doctor for a specific patient.")
    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    public ResponseEntity<List<DoctorNoteResponse>> getDoctorNotesByDoctorAndPatient(
            @PathVariable Long doctorId,
            @PathVariable Long patientId) {
        List<DoctorNoteResponse> responses = doctorNoteService.getDoctorNotesByDoctorAndPatient(doctorId, patientId);
        return ResponseEntity.ok(responses);
    }
}

