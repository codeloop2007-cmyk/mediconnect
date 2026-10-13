package com.mediconnect.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mediconnect.backend.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class AppointmentResponse {
    public AppointmentResponse() {}
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
}

