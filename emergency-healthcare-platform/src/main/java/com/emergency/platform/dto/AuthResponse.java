package com.emergency.platform.dto;

public record AuthResponse(
        String token,
        String role,
        String name,
        boolean otpRequired,
        String message
) {
}
