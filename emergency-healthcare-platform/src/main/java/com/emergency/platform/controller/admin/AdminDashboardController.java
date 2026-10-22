package com.emergency.platform.controller.admin;

import com.emergency.platform.entity.Hospital;
import com.emergency.platform.repository.EmergencyCaseRepository;
import com.emergency.platform.repository.HospitalRepository;
import com.emergency.platform.repository.PatientTransferRepository;
import com.emergency.platform.repository.PlannedAdmissionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {
    private final HospitalRepository hospitalRepository;
    private final EmergencyCaseRepository emergencyCaseRepository;
    private final PlannedAdmissionRepository admissionRepository;
    private final PatientTransferRepository transferRepository;

    public AdminDashboardController(HospitalRepository hospitalRepository,
                                    EmergencyCaseRepository emergencyCaseRepository,
                                    PlannedAdmissionRepository admissionRepository,
                                    PatientTransferRepository transferRepository) {
        this.hospitalRepository = hospitalRepository;
        this.emergencyCaseRepository = emergencyCaseRepository;
        this.admissionRepository = admissionRepository;
        this.transferRepository = transferRepository;
    }

    @GetMapping("/hospitals")
    public List<Hospital> hospitals() {
        return hospitalRepository.findAll();
    }

    @GetMapping("/reports")
    public Map<String, Object> reports() {
        return Map.of(
                "hospitals", hospitalRepository.count(),
                "emergencies", emergencyCaseRepository.count(),
                "admissions", admissionRepository.count(),
                "transfers", transferRepository.count(),
                "transferMetrics", "available after transfer workflow activity"
        );
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        return Map.of(
                "totalHospitals", hospitals.size(),
                "totalEmergencies", emergencyCaseRepository.count(),
                "totalAdmissions", admissionRepository.count(),
                "totalTransfers", transferRepository.count(),
                "hospitals", hospitals.stream().map(h -> Map.of(
                        "id", h.getId(),
                        "name", h.getName(),
                        "city", h.getCity(),
                        "totalBeds", h.getTotalBeds(),
                        "availableBeds", h.getAvailableBeds(),
                        "activeCases", h.getActiveCases(),
                        "availableAmbulances", h.getAvailableAmbulances()
                )).toList()
        );
    }
}
