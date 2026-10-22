package com.emergency.platform.dto;

import java.util.List;

public record HospitalDashboardResponse(
        List<EmergencyCaseResponse> emergencyQueue,
        List<AdmissionResponse> plannedAdmissionsQueue,
        List<String> availableDoctors,
        List<String> availableBeds,
        List<String> availableAmbulances,
        List<TimelineItem> notifications,
        int activeEmergencies,
        int plannedAdmissions,
        int transfers,
        int completedCases
) {
}
