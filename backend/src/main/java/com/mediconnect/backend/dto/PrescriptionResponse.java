package com.mediconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class PrescriptionResponse {
    public PrescriptionResponse() {}
    private Long prescriptionId;
    private Long consultationId;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String details;
    private LocalDateTime createdAt;
}

