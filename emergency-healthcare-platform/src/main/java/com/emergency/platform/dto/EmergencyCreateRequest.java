package com.emergency.platform.dto;

import com.emergency.platform.entity.EmergencyType;
import com.emergency.platform.entity.SeverityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmergencyCreateRequest(
        @NotBlank String name,
        @NotBlank String phone,
        String email,
        @NotNull EmergencyType emergencyType,
        @NotNull SeverityLevel severity,
        @NotBlank String locationText,
        Double latitude,
        Double longitude
) {
}
