package com.emergency.platform.controller.admission;

import com.emergency.platform.dto.AdmissionCreateRequest;
import com.emergency.platform.dto.AdmissionResponse;
import com.emergency.platform.service.admission.AdmissionWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/admissions")
public class PublicAdmissionController {
    private final AdmissionWorkflowService admissionWorkflowService;

    public PublicAdmissionController(AdmissionWorkflowService admissionWorkflowService) {
        this.admissionWorkflowService = admissionWorkflowService;
    }

    @PostMapping("/otp")
    public Map<String, String> startOtp(@RequestParam String target) {
        String otp = admissionWorkflowService.startOtp(target, "ADMISSION");
        return Map.of("message", "OTP generated for admission workflow", "demoOtp", otp);
    }

    @PostMapping
    public AdmissionResponse submit(@Valid @RequestBody AdmissionCreateRequest request) {
        return admissionWorkflowService.submit(request);
    }
}
