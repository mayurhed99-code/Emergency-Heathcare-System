package com.emergency.platform.controller.driver;

import com.emergency.platform.dto.EmergencyCaseResponse;
import com.emergency.platform.service.emergency.EmergencyWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/driver")
public class DriverDashboardController {
    private final EmergencyWorkflowService emergencyWorkflowService;

    public DriverDashboardController(EmergencyWorkflowService emergencyWorkflowService) {
        this.emergencyWorkflowService = emergencyWorkflowService;
    }

    @GetMapping("/assignments")
    public List<EmergencyCaseResponse> assignments(Principal principal) {
        return emergencyWorkflowService.queue();
    }
}
