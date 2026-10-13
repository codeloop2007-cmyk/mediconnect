package com.mediconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long doctorId;
    private String name;
    private String specialization;
    private String qualification;
    private String language;
    private String location;
    private BigDecimal consultationFee;
    private Double averageRating;
    private Integer totalReviews;
}

