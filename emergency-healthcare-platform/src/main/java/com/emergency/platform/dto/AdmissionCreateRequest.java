package com.emergency.platform.dto;

import com.emergency.platform.entity.Specialization;
import com.emergency.platform.entity.VerificationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdmissionCreateRequest(
        @NotBlank String name,
        @NotBlank String phone,
        String email,
        @NotBlank String symptoms,
        @NotNull Specialization preferredSpecialization,
        @NotNull VerificationMethod verificationMethod,
        @NotBlank String otp,
        String locationText
) {
}
