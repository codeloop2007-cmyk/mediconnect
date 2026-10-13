package com.mediconnect.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class DoctorRequest {
    public DoctorRequest() {}

    @NotBlank(message = "Doctor name is required")
    private String name;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    private String qualification;

    private String language;

    private String location;

    @NotNull(message = "Consultation fee is required")
    @PositiveOrZero(message = "Consultation fee must be zero or positive")
    private BigDecimal consultationFee;
}

