package com.emergency.platform.dto;

public record AdmissionResponse(
        String trackingCode,
        String patientName,
        String status,
        String hospital,
        String doctor,
        String bed,
        String confirmationMessage
) {
}
