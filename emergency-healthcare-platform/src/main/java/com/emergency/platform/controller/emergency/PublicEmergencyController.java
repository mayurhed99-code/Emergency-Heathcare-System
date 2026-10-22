package com.emergency.platform.controller.emergency;

import com.emergency.platform.dto.EmergencyCaseResponse;
import com.emergency.platform.dto.EmergencyCreateRequest;
import com.emergency.platform.dto.HospitalCandidate;
import com.emergency.platform.entity.EmergencyType;
import com.emergency.platform.service.emergency.EmergencyWorkflowService;
import com.emergency.platform.service.hospital.HospitalMatchingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/emergencies")
public class PublicEmergencyController {
    private final EmergencyWorkflowService emergencyWorkflowService;
    private final HospitalMatchingService hospitalMatchingService;

    public PublicEmergencyController(EmergencyWorkflowService emergencyWorkflowService, HospitalMatchingService hospitalMatchingService) {
        this.emergencyWorkflowService = emergencyWorkflowService;
        this.hospitalMatchingService = hospitalMatchingService;
    }

    @PostMapping
    public EmergencyCaseResponse create(@Valid @RequestBody EmergencyCreateRequest request) {
        return emergencyWorkflowService.createEmergency(request);
    }

    @GetMapping("/tracking/{trackingCode}")
    public EmergencyCaseResponse track(@PathVariable String trackingCode) {
        return emergencyWorkflowService.track(trackingCode);
    }

    @GetMapping("/nearby-hospitals")
    public List<HospitalCandidate> nearby(@RequestParam EmergencyType emergencyType, @RequestParam String locationText) {
        return hospitalMatchingService.generateNearbyHospitals(emergencyType, locationText);
    }
}
