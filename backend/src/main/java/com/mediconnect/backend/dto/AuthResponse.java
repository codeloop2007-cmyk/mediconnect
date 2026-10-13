package com.mediconnect.backend.dto;

import com.mediconnect.backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private Long profileId; // patientId or doctorId if applicable
}

