package com.emergency.platform.controller.doctor;

import com.emergency.platform.dto.EmergencyCaseResponse;
import com.emergency.platform.entity.CaseStatus;
import com.emergency.platform.service.emergency.EmergencyWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorDashboardController {
    private final EmergencyWorkflowService emergencyWorkflowService;

    public DoctorDashboardController(EmergencyWorkflowService emergencyWorkflowService) {
        this.emergencyWorkflowService = emergencyWorkflowService;
    }

    @GetMapping("/cases")
    public List<EmergencyCaseResponse> cases(Principal principal) {
        return emergencyWorkflowService.queue();
    }

    @PatchMapping("/cases/{trackingCode}/status")
    public EmergencyCaseResponse updateStatus(@PathVariable String trackingCode, @RequestParam CaseStatus status) {
        return emergencyWorkflowService.updateTreatment(trackingCode, status);
    }
}
