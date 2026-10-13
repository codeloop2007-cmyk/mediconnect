package com.mediconnect.backend.controller;

import com.mediconnect.backend.dto.AppointmentRequest;
import com.mediconnect.backend.dto.AppointmentResponse;
import com.mediconnect.backend.dto.AppointmentStatusUpdateRequest;
import com.mediconnect.backend.service.AppointmentService;
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
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Endpoints for scheduling, rescheduling, cancelling, and viewing consultations")
@SecurityRequirement(name = "BearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Book an appointment", description = "Books a consultation slot. Validates against double-booking conflicts.")
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update appointment status", description = "Update appointment status (COMPLETED, CANCELLED, RESCHEDULED with new date/time).")
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        AppointmentResponse response = appointmentService.updateAppointmentStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get appointment by ID", description = "Retrieves details of a specific appointment.")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get appointments for patient", description = "Retrieves all consultation appointments booked by a patient.")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get appointments for doctor", description = "Retrieves all consultation appointments assigned to a doctor.")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        List<AppointmentResponse> responses = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(responses);
    }
}

