package com.emergency.platform.service.hospital;

import com.emergency.platform.dto.HospitalCandidate;
import com.emergency.platform.dto.TransferRequest;
import com.emergency.platform.entity.CaseStatus;
import com.emergency.platform.entity.EmergencyCase;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.PatientTransfer;
import com.emergency.platform.entity.TransferStatus;
import com.emergency.platform.exception.ResourceNotFoundException;
import com.emergency.platform.repository.HospitalRepository;
import com.emergency.platform.repository.PatientTransferRepository;
import com.emergency.platform.service.emergency.EmergencyWorkflowService;
import com.emergency.platform.service.notification.NotificationOrchestrator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransferWorkflowService {
    private final PatientTransferRepository transferRepository;
    private final HospitalRepository hospitalRepository;
    private final EmergencyWorkflowService emergencyWorkflowService;
    private final HospitalMatchingService matchingService;
    private final NotificationOrchestrator notifications;

    public TransferWorkflowService(PatientTransferRepository transferRepository,
                                   HospitalRepository hospitalRepository,
                                   EmergencyWorkflowService emergencyWorkflowService,
                                   HospitalMatchingService matchingService,
                                   NotificationOrchestrator notifications) {
        this.transferRepository = transferRepository;
        this.hospitalRepository = hospitalRepository;
        this.emergencyWorkflowService = emergencyWorkflowService;
        this.matchingService = matchingService;
        this.notifications = notifications;
    }

    public List<HospitalCandidate> suggestions(String emergencyTrackingCode) {
        EmergencyCase emergency = emergencyWorkflowService.findCase(emergencyTrackingCode);
        return matchingService.generateNearbyHospitals(emergency.getEmergencyType(), emergency.getPatient().getLocationText())
                .stream()
                .filter(candidate -> !candidate.hospitalId().equals(emergency.getHospital().getId()))
                .toList();
    }

    @Transactional
    public PatientTransfer create(String emergencyTrackingCode, TransferRequest request) {
        EmergencyCase emergency = emergencyWorkflowService.findCase(emergencyTrackingCode);
        Hospital receivingHospital = request.receivingHospitalId() == null
                ? hospitalRepository.findById(suggestions(emergencyTrackingCode).get(0).hospitalId()).orElseThrow()
                : hospitalRepository.findById(request.receivingHospitalId()).orElseThrow(() -> new ResourceNotFoundException("Receiving hospital not found"));
        PatientTransfer transfer = new PatientTransfer();
        transfer.setEmergencyCase(emergency);
        transfer.setFromHospital(emergency.getHospital());
        transfer.setToHospital(receivingHospital);
        transfer.setReason(request.reason());
        transfer.setDetails(request.details());
        transfer.setStatus(TransferStatus.RECEIVING_HOSPITAL_ALERTED);
        transferRepository.save(transfer);
        emergencyWorkflowService.updateTreatment(emergencyTrackingCode, CaseStatus.TRANSFER_REQUESTED);
        notifications.dashboard(receivingHospital, com.emergency.platform.entity.Role.HOSPITAL, null, "Transfer requested", "Patient transfer for " + emergencyTrackingCode + " needs review.", emergencyTrackingCode);
        notifications.hospitalEmergencyAlert(receivingHospital, emergencyTrackingCode);
        return transfer;
    }
}
