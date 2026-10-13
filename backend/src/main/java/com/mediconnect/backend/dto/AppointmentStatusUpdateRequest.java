package com.mediconnect.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mediconnect.backend.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusUpdateRequest {

    @NotNull(message = "Status is required (SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED)")
    private AppointmentStatus status;

    // Optional fields for rescheduling
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime newTime;
}

