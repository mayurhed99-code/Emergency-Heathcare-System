package com.emergency.platform.controller.hospital;

import com.emergency.platform.dto.AllocateBedRequest;
import com.emergency.platform.dto.AssignDoctorRequest;
import com.emergency.platform.dto.EmergencyCaseResponse;
import com.emergency.platform.dto.HospitalCandidate;
import com.emergency.platform.dto.HospitalDashboardResponse;
import com.emergency.platform.dto.SendNotificationRequest;
import com.emergency.platform.dto.TransferRequest;
import com.emergency.platform.entity.CaseStatus;
import com.emergency.platform.entity.PatientTransfer;
import com.emergency.platform.service.admission.AdmissionWorkflowService;
import com.emergency.platform.service.emergency.EmergencyWorkflowService;
import com.emergency.platform.service.hospital.HospitalDashboardService;
import com.emergency.platform.service.hospital.TransferWorkflowService;
import com.emergency.platform.service.notification.NotificationOrchestrator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/hospital")
public class HospitalDashboardController {
    private final HospitalDashboardService dashboardService;
    private final EmergencyWorkflowService emergencyWorkflowService;
    private final AdmissionWorkflowService admissionWorkflowService;
    private final TransferWorkflowService transferWorkflowService;
    private final NotificationOrchestrator notifications;

    public HospitalDashboardController(HospitalDashboardService dashboardService,
                                       EmergencyWorkflowService emergencyWorkflowService,
                                       AdmissionWorkflowService admissionWorkflowService,
                                       TransferWorkflowService transferWorkflowService,
                                       NotificationOrchestrator notifications) {
        this.dashboardService = dashboardService;
        this.emergencyWorkflowService = emergencyWorkflowService;
        this.admissionWorkflowService = admissionWorkflowService;
        this.transferWorkflowService = transferWorkflowService;
        this.notifications = notifications;
    }

    @GetMapping("/dashboard")
    public HospitalDashboardResponse dashboard(@RequestParam(defaultValue = "1") Long hospitalId, Principal principal) {
        return dashboardService.dashboardForUser(principal.getName(), hospitalId);
    }

    @PostMapping("/emergencies/{trackingCode}/accept")
    public EmergencyCaseResponse acceptEmergency(@PathVariable String trackingCode) {
        return emergencyWorkflowService.acceptEmergency(trackingCode);
    }

    @PostMapping("/emergencies/{trackingCode}/assign-doctor")
    public EmergencyCaseResponse assignDoctor(@PathVariable String trackingCode, @RequestBody AssignDoctorRequest request) {
        return emergencyWorkflowService.assignDoctor(trackingCode, request);
    }

    @PostMapping("/emergencies/{trackingCode}/allocate-bed")
    public EmergencyCaseResponse allocateBed(@PathVariable String trackingCode, @RequestBody AllocateBedRequest request) {
        return emergencyWorkflowService.allocateBed(trackingCode, request);
    }

    @PatchMapping("/emergencies/{trackingCode}/status")
    public EmergencyCaseResponse updateStatus(@PathVariable String trackingCode, @RequestParam CaseStatus status) {
        return emergencyWorkflowService.updateTreatment(trackingCode, status);
    }

    @PostMapping("/admissions/{trackingCode}/approve")
    public Object approveAdmission(@PathVariable String trackingCode) {
        return admissionWorkflowService.approve(trackingCode);
    }

    @PostMapping("/admissions/{trackingCode}/assign-doctor")
    public Object assignAdmissionDoctor(@PathVariable String trackingCode, @RequestBody AssignDoctorRequest request) {
        return admissionWorkflowService.assignDoctor(trackingCode, request);
    }

    @PostMapping("/admissions/{trackingCode}/allocate-bed")
    public Object allocateAdmissionBed(@PathVariable String trackingCode, @RequestBody AllocateBedRequest request) {
        return admissionWorkflowService.allocateBed(trackingCode, request);
    }

    @GetMapping("/emergencies/{trackingCode}/transfer-suggestions")
    public List<HospitalCandidate> transferSuggestions(@PathVariable String trackingCode) {
        return transferWorkflowService.suggestions(trackingCode);
    }

    @PostMapping("/emergencies/{trackingCode}/transfer")
    public PatientTransfer transferPatient(@PathVariable String trackingCode, @Valid @RequestBody TransferRequest request) {
        return transferWorkflowService.create(trackingCode, request);
    }

    @PostMapping("/notifications/send")
    public Map<String, String> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        notifications.custom(null, request.referenceCode(), request.title(), request.message(), request.recipient());
        return Map.of("message", "Notification sent");
    }
}
