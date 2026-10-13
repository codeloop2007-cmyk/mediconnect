package com.mediconnect.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {

    @NotNull(message = "Consultation ID is required")
    private Long consultationId;

    @NotBlank(message = "Prescription details cannot be blank")
    private String details;
}

