package com.mediconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class DoctorNoteResponse {
    public DoctorNoteResponse() {}
    private Long noteId;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private String note;
    private LocalDateTime createdAt;
}

