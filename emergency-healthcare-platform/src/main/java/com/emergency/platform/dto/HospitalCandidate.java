package com.emergency.platform.dto;

public record HospitalCandidate(
        Long hospitalId,
        String name,
        String city,
        String specializationMatch,
        int availableBeds,
        int availableAmbulances,
        double simulatedDistanceKm,
        double score
) {
}
