package com.emergency.platform.service.hospital;

import com.emergency.platform.dto.HospitalCandidate;
import com.emergency.platform.entity.EmergencyType;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.Specialization;
import com.emergency.platform.exception.WorkflowException;
import com.emergency.platform.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
public class HospitalMatchingService {
    private final HospitalRepository hospitalRepository;

    public HospitalMatchingService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public List<HospitalCandidate> generateNearbyHospitals(EmergencyType emergencyType, String locationText) {
        Specialization required = specializationFor(emergencyType);
        List<HospitalCandidate> candidates = hospitalRepository.findAll()
                .stream()
                .map(hospital -> candidate(hospital, required, locationText))
                .sorted(Comparator.comparingDouble(HospitalCandidate::score))
                .limit(7)
                .toList();
        if (candidates.size() < 5) {
            return candidates;
        }
        return candidates.subList(0, Math.min(7, candidates.size()));
    }

    public Hospital selectBestHospital(EmergencyType emergencyType, String locationText) {
        Specialization required = specializationFor(emergencyType);
        List<Hospital> allHospitals = hospitalRepository.findAll();
        
        // Strict validation: must have specialization, bed, and ambulance
        List<Hospital> eligible = allHospitals.stream()
                .filter(h -> h.getSpecializations().contains(required) && h.getAvailableBeds() > 0 && h.getAvailableAmbulances() > 0)
                .toList();
                
        // Fallback for demo guarantee if no hospital strictly matches
        if (eligible.isEmpty()) {
            eligible = allHospitals;
        }

        return eligible.stream()
                .min(Comparator.comparingDouble(h -> simulatedDistance(h, locationText)))
                .orElseThrow(() -> new WorkflowException("No hospitals are available for this emergency"));
    }

    public double simulatedDistance(Hospital hospital, String locationText) {
        Random random = new Random((hospital.getName() + "|" + locationText).hashCode());
        return Math.round((1.4 + random.nextDouble(18.6)) * 10.0) / 10.0;
    }

    public Specialization specializationFor(EmergencyType emergencyType) {
        return switch (emergencyType) {
            case HEART_ATTACK -> Specialization.CARDIOLOGY;
            case BRAIN_INJURY -> Specialization.NEUROLOGY;
            case BURN_INJURY -> Specialization.BURN_UNIT;
            case ROAD_ACCIDENT -> Specialization.TRAUMA_CENTER;
            case BREATHING_PROBLEM -> Specialization.ICU;
            case PREGNANCY_EMERGENCY, GENERAL_CRITICAL -> Specialization.EMERGENCY_MEDICINE;
        };
    }

    private HospitalCandidate candidate(Hospital hospital, Specialization required, String locationText) {
        double distance = simulatedDistance(hospital, locationText);
        boolean specializationMatch = hospital.getSpecializations().contains(required);
        double score = distance
                + (specializationMatch ? 0 : 12)
                + (hospital.getAvailableBeds() > 0 ? 0 : 20)
                + (hospital.getAvailableAmbulances() > 0 ? 0 : 6)
                + hospital.getActiveCases() * 0.7;
        return new HospitalCandidate(
                hospital.getId(),
                hospital.getName(),
                hospital.getCity(),
                specializationMatch ? required.name() : "NEAREST_GENERAL_SUPPORT",
                hospital.getAvailableBeds(),
                hospital.getAvailableAmbulances(),
                distance,
                Math.round(score * 10.0) / 10.0
        );
    }
}
