package com.mediconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class DoctorResponse {
    public DoctorResponse() {}
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

