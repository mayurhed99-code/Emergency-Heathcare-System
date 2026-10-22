package com.emergency.platform.dto;

import java.util.List;

public record EmergencyCaseResponse(
        String trackingCode,
        String patientName,
        String phone,
        String emergencyType,
        String severity,
        String status,
        String hospital,
        String assignedAmbulance,
        String driver,
        String doctor,
        String bed,
        double hospitalDistanceKm,
        double ambulanceDistanceKm,
        List<TimelineItem> notificationTimeline
) {
}
