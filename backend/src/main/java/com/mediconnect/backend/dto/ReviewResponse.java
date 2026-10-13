package com.mediconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ReviewResponse {
    public ReviewResponse() {}
    private Long reviewId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

