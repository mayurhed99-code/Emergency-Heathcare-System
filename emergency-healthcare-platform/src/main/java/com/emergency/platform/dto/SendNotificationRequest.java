package com.emergency.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record SendNotificationRequest(
        @NotBlank String referenceCode,
        @NotBlank String title,
        @NotBlank String message,
        String recipient
) {
}
