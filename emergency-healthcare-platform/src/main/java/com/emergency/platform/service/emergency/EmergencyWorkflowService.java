package com.emergency.platform.service.emergency;

import com.emergency.platform.dto.AllocateBedRequest;
import com.emergency.platform.dto.AssignDoctorRequest;
import com.emergency.platform.dto.EmergencyCaseResponse;
import com.emergency.platform.dto.EmergencyCreateRequest;
import com.emergency.platform.entity.Ambulance;
import com.emergency.platform.entity.AmbulanceStatus;
import com.emergency.platform.entity.Bed;
import com.emergency.platform.entity.BedStatus;
import com.emergency.platform.entity.CaseStatus;
import com.emergency.platform.entity.Doctor;
import com.emergency.platform.entity.Driver;
import com.emergency.platform.entity.EmergencyCase;
import com.emergency.platform.entity.EmergencyStatusHistory;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.Patient;
import com.emergency.platform.entity.Role;
import com.emergency.platform.exception.ResourceNotFoundException;
import com.emergency.platform.exception.WorkflowException;
import com.emergency.platform.mapper.WorkflowMapper;
import com.emergency.platform.repository.AmbulanceRepository;
import com.emergency.platform.repository.BedRepository;
import com.emergency.platform.repository.DoctorRepository;
import com.emergency.platform.repository.DriverRepository;
import com.emergency.platform.repository.EmergencyCaseRepository;
import com.emergency.platform.repository.EmergencyStatusHistoryRepository;
import com.emergency.platform.repository.PatientRepository;
import com.emergency.platform.repository.DoctorRequestRepository;
import com.emergency.platform.entity.DoctorRequest;
import com.emergency.platform.entity.Specialization;
import com.emergency.platform.service.hospital.HospitalMatchingService;
import com.emergency.platform.service.notification.NotificationOrchestrator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EmergencyWorkflowService {
    private final PatientRepository patientRepository;
    private final EmergencyCaseRepository emergencyRepository;
    private final EmergencyStatusHistoryRepository historyRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final DriverRepository driverRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final DoctorRequestRepository doctorRequestRepository;
    private final HospitalMatchingService hospitalMatchingService;
    private final NotificationOrchestrator notifications;
    private final WorkflowMapper mapper;

    public EmergencyWorkflowService(PatientRepository patientRepository,
                                    EmergencyCaseRepository emergencyRepository,
                                    EmergencyStatusHistoryRepository historyRepository,
                                    AmbulanceRepository ambulanceRepository,
                                    DriverRepository driverRepository,
                                    DoctorRepository doctorRepository,
                                    BedRepository bedRepository,
                                    DoctorRequestRepository doctorRequestRepository,
                                    HospitalMatchingService hospitalMatchingService,
                                    NotificationOrchestrator notifications,
                                    WorkflowMapper mapper) {
        this.patientRepository = patientRepository;
        this.emergencyRepository = emergencyRepository;
        this.historyRepository = historyRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.driverRepository = driverRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.doctorRequestRepository = doctorRequestRepository;
        this.hospitalMatchingService = hospitalMatchingService;
        this.notifications = notifications;
        this.mapper = mapper;
    }

    @Transactional
    public EmergencyCaseResponse createEmergency(EmergencyCreateRequest request) {
        Patient patient = new Patient();
        patient.setName(request.name());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setLocationText(request.locationText());
        patient.setLatitude(request.latitude());
        patient.setLongitude(request.longitude());
        patientRepository.save(patient);

        Hospital hospital = hospitalMatchingService.selectBestHospital(request.emergencyType(), request.locationText());
        Ambulance ambulance = ambulanceRepository.findFirstByHospitalAndStatus(hospital, AmbulanceStatus.AVAILABLE)
                .or(() -> ambulanceRepository.findFirstByStatus(AmbulanceStatus.AVAILABLE))
                .orElseThrow(() -> new WorkflowException("No ambulance is available"));
        Driver driver = ambulance.getDriver() != null
                ? ambulance.getDriver()
                : driverRepository.findFirstByAvailableTrue().orElseThrow(() -> new WorkflowException("No driver is available"));

        EmergencyCase emergency = new EmergencyCase();
        emergency.setTrackingCode("EMG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        emergency.setPatient(patient);
        emergency.setEmergencyType(request.emergencyType());
        emergency.setSeverity(request.severity());
        emergency.setHospital(hospital);
        emergency.setAmbulance(ambulance);
        emergency.setDriver(driver);
        emergency.setSimulatedHospitalDistanceKm(hospitalMatchingService.simulatedDistance(hospital, request.locationText()));
        emergency.setSimulatedAmbulanceDistanceKm(Math.max(0.8, Math.round((emergency.getSimulatedHospitalDistanceKm() * 0.45) * 10.0) / 10.0));
        emergency.setStatus(CaseStatus.AMBULANCE_ASSIGNED);
        emergencyRepository.save(emergency);

        ambulance.setStatus(AmbulanceStatus.DISPATCHED);
        driver.setAvailable(false);
        hospital.setAvailableAmbulances(Math.max(0, hospital.getAvailableAmbulances() - 1));
        hospital.setActiveCases(hospital.getActiveCases() + 1);
        ambulanceRepository.save(ambulance);
        driverRepository.save(driver);

        history(emergency, CaseStatus.CREATED, "Citizen submitted emergency request");
        history(emergency, CaseStatus.HOSPITAL_ALERT_SENT, "Hospital selected from strict resource validation");
        notifications.hospitalEmergencyAlert(hospital, emergency.getTrackingCode());
        history(emergency, CaseStatus.AMBULANCE_ASSIGNED, "Ambulance " + ambulance.getCallSign() + " assigned with driver " + driver.getName());
        history(emergency, CaseStatus.DRIVER_ALERT_SENT, "Nearest available ambulance and driver assigned");
        notifications.driverAssignment(driver.getPhone(), driver.getEmail(), emergency.getTrackingCode());
        notifications.patientEmergencyConfirmation(patient.getPhone(), patient.getEmail(), emergency.getTrackingCode(), hospital.getName());

        Specialization spec = hospitalMatchingService.specializationFor(request.emergencyType());
        boolean hasLocalDoctor = doctorRepository.findFirstByHospitalAndSpecializationAndAvailableTrue(hospital, spec).isPresent();
        if (!hasLocalDoctor) {
            // Fallback Workflow: Request External Specialist
            Doctor externalDoc = doctorRepository.findAll().stream()
                    .filter(d -> d.getSpecialization() == spec)
                    .findFirst()
                    .orElse(null);

            if (externalDoc != null) {
                DoctorRequest doctorRequest = new DoctorRequest();
                doctorRequest.setDoctor(externalDoc);
                doctorRequest.setHospital(hospital);
                doctorRequest.setEmergencyCase(emergency);
                doctorRequest.setStatus("ACCEPTED"); // Auto-accept for simulation
                doctorRequestRepository.save(doctorRequest);

                emergency.setStatus(CaseStatus.DOCTOR_REQUESTED);
                emergencyRepository.save(emergency);

                history(emergency, CaseStatus.DOCTOR_REQUESTED, "External specialist requested: " + externalDoc.getName());
                
                notifications.custom(hospital, emergency.getTrackingCode(), "External Specialist Auto-Assigned",
                        "Dr. " + externalDoc.getName() + " from another facility has accepted the request.", hospital.getContactEmail());
                notifications.custom(null, emergency.getTrackingCode(), "External Case Request",
                        "You have been requested at " + hospital.getName() + " for case " + emergency.getTrackingCode(), externalDoc.getEmail());
            }
        }

        return mapper.toEmergencyResponse(emergency);
    }

    public EmergencyCaseResponse track(String trackingCode) {
        return mapper.toEmergencyResponse(findCase(trackingCode));
    }

    public List<EmergencyCaseResponse> queue() {
        return emergencyRepository.findByStatusInOrderByCreatedAtDesc(List.of(
                        CaseStatus.AMBULANCE_ASSIGNED,
                        CaseStatus.ACCEPTED,
                        CaseStatus.DOCTOR_ASSIGNED,
                        CaseStatus.BED_ALLOCATED,
                        CaseStatus.UNDER_TREATMENT
                ))
                .stream()
                .map(mapper::toEmergencyResponse)
                .toList();
    }

    @Transactional
    public EmergencyCaseResponse acceptEmergency(String trackingCode) {
        EmergencyCase emergency = findCase(trackingCode);
        if (emergency.getStatus() != CaseStatus.AMBULANCE_ASSIGNED && emergency.getStatus() != CaseStatus.DOCTOR_REQUESTED) {
            throw new WorkflowException("Emergency must be in AMBULANCE_ASSIGNED or DOCTOR_REQUESTED state to accept");
        }
        emergency.setStatus(CaseStatus.ACCEPTED);
        emergency.setUpdatedAt(Instant.now());
        history(emergency, CaseStatus.ACCEPTED, "Hospital staff accepted emergency case");

        // Notify all related entities
        notifications.hospitalStepNotification(emergency.getHospital(), trackingCode,
                "Emergency Accepted", "Case " + trackingCode + " has been accepted by hospital staff.");
        notifications.patientStatusUpdate(
                emergency.getPatient().getPhone(), emergency.getPatient().getEmail(),
                trackingCode, "Your emergency has been accepted by " + emergency.getHospital().getName() + ". Medical team is preparing.");
        if (emergency.getDriver() != null) {
            notifications.driverAssignment(emergency.getDriver().getPhone(), emergency.getDriver().getEmail(), trackingCode);
        }

        return mapper.toEmergencyResponse(emergencyRepository.save(emergency));
    }

    @Transactional
    public EmergencyCaseResponse assignDoctor(String trackingCode, AssignDoctorRequest request) {
        EmergencyCase emergency = findCase(trackingCode);
        if (emergency.getStatus() != CaseStatus.ACCEPTED) {
            throw new WorkflowException("Emergency must be ACCEPTED by hospital staff before assigning a doctor");
        }
        Doctor doctor;
        if (request.doctorId() != null) {
            doctor = doctorRepository.findById(request.doctorId()).orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        } else {
            var externalReq = doctorRequestRepository.findFirstByEmergencyCaseAndStatus(emergency, "ACCEPTED");
            if (externalReq.isPresent()) {
                doctor = externalReq.get().getDoctor();
            } else {
                doctor = doctorRepository.findFirstByHospitalAndSpecializationAndAvailableTrue(emergency.getHospital(), hospitalMatchingService.specializationFor(emergency.getEmergencyType()))
                        .orElseThrow(() -> new WorkflowException("No matching doctor is available"));
            }
        }
        emergency.setDoctor(doctor);
        emergency.setStatus(CaseStatus.DOCTOR_ASSIGNED);
        emergency.setUpdatedAt(Instant.now());
        doctor.setAvailable(false);
        doctorRepository.save(doctor);
        history(emergency, CaseStatus.DOCTOR_ALERT_SENT, "Doctor alert sent");
        history(emergency, CaseStatus.DOCTOR_ASSIGNED, "Doctor " + doctor.getName() + " assigned");

        // Notify all related entities
        notifications.dashboard(emergency.getHospital(), Role.DOCTOR, null, "Doctor assigned",
                "Case " + trackingCode + " assigned to " + doctor.getName() + ".", trackingCode);
        notifications.doctorAssignment(doctor.getPhone(), doctor.getEmail(), emergency.getTrackingCode());
        notifications.patientStatusUpdate(
                emergency.getPatient().getPhone(), emergency.getPatient().getEmail(),
                trackingCode, "Doctor " + doctor.getName() + " has been assigned to your case.");
        if (emergency.getDriver() != null) {
            notifications.patientStatusUpdate(emergency.getDriver().getPhone(), emergency.getDriver().getEmail(),
                    trackingCode, "Doctor " + doctor.getName() + " assigned to case " + trackingCode);
        }

        return mapper.toEmergencyResponse(emergencyRepository.save(emergency));
    }

    @Transactional
    public EmergencyCaseResponse allocateBed(String trackingCode, AllocateBedRequest request) {
        EmergencyCase emergency = findCase(trackingCode);
        if (emergency.getStatus() != CaseStatus.DOCTOR_ASSIGNED) {
            throw new WorkflowException("Doctor must be assigned first before allocating a bed");
        }
        Bed bed = request.bedId() == null
                ? bedRepository.findFirstByHospitalAndStatus(emergency.getHospital(), BedStatus.AVAILABLE)
                .orElseThrow(() -> new WorkflowException("No bed is available"))
                : bedRepository.findById(request.bedId()).orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        bed.setStatus(BedStatus.RESERVED);
        emergency.setBed(bed);
        emergency.setStatus(CaseStatus.BED_ALLOCATED);
        emergency.setUpdatedAt(Instant.now());
        emergency.getHospital().setAvailableBeds(Math.max(0, emergency.getHospital().getAvailableBeds() - 1));
        bedRepository.save(bed);
        history(emergency, CaseStatus.BED_ALLOCATED, "Bed " + bed.getBedCode() + " allocated by hospital staff");

        // Notify all related entities
        notifications.hospitalStepNotification(emergency.getHospital(), trackingCode,
                "Bed Allocated", "Bed " + bed.getBedCode() + " allocated for case " + trackingCode + ".");
        notifications.patientStatusUpdate(
                emergency.getPatient().getPhone(), emergency.getPatient().getEmail(),
                trackingCode, "Bed " + bed.getBedCode() + " has been allocated for you at " + emergency.getHospital().getName() + ".");
        if (emergency.getDoctor() != null) {
            notifications.patientStatusUpdate(emergency.getDoctor().getPhone(), emergency.getDoctor().getEmail(),
                    trackingCode, "Bed " + bed.getBedCode() + " allocated for case " + trackingCode);
        }
        if (emergency.getDriver() != null) {
            notifications.patientStatusUpdate(emergency.getDriver().getPhone(), emergency.getDriver().getEmail(),
                    trackingCode, "Bed allocated. Proceed to deliver patient to " + emergency.getHospital().getName());
        }

        return mapper.toEmergencyResponse(emergencyRepository.save(emergency));
    }

    @Transactional
    public EmergencyCaseResponse updateTreatment(String trackingCode, CaseStatus status) {
        EmergencyCase emergency = findCase(trackingCode);
        if (status == CaseStatus.UNDER_TREATMENT && emergency.getStatus() != CaseStatus.BED_ALLOCATED) {
            throw new WorkflowException("Bed must be allocated before starting treatment");
        }
        if (status == CaseStatus.DISCHARGED && emergency.getStatus() != CaseStatus.UNDER_TREATMENT && emergency.getStatus() != CaseStatus.STABLE && emergency.getStatus() != CaseStatus.CRITICAL) {
            throw new WorkflowException("Patient must be under treatment to be discharged");
        }
        emergency.setStatus(status);
        emergency.setUpdatedAt(Instant.now());
        history(emergency, status, "Treatment status updated to " + status.name());

        // Release resources if patient is discharged
        if (status == CaseStatus.DISCHARGED) {
            if (emergency.getBed() != null) {
                Bed bed = emergency.getBed();
                if (bed.getStatus() != BedStatus.AVAILABLE) {
                    bed.setStatus(BedStatus.AVAILABLE);
                    bedRepository.save(bed);
                    emergency.getHospital().setAvailableBeds(emergency.getHospital().getAvailableBeds() + 1);
                }
            }
            if (emergency.getDoctor() != null) {
                Doctor doctor = emergency.getDoctor();
                if (!doctor.isAvailable()) {
                    doctor.setAvailable(true);
                    doctorRepository.save(doctor);
                }
            }
            if (emergency.getAmbulance() != null) {
                Ambulance ambulance = emergency.getAmbulance();
                if (ambulance.getStatus() != AmbulanceStatus.AVAILABLE) {
                    ambulance.setStatus(AmbulanceStatus.AVAILABLE);
                    ambulanceRepository.save(ambulance);
                    emergency.getHospital().setAvailableAmbulances(emergency.getHospital().getAvailableAmbulances() + 1);
                }
            }
            if (emergency.getDriver() != null) {
                Driver driver = emergency.getDriver();
                if (!driver.isAvailable()) {
                    driver.setAvailable(true);
                    driverRepository.save(driver);
                }
            }
            if (emergency.getHospital() != null) {
                emergency.getHospital().setActiveCases(Math.max(0, emergency.getHospital().getActiveCases() - 1));
            }
        }

        // Notify all related entities about status change
        String statusLabel = switch (status) {
            case UNDER_TREATMENT -> "Treatment has started";
            case CRITICAL -> "Patient marked as CRITICAL";
            case STABLE -> "Patient condition is STABLE";
            case DISCHARGED -> "Patient has been DISCHARGED";
            default -> "Status updated to " + status.name();
        };

        notifications.hospitalStepNotification(emergency.getHospital(), trackingCode,
                "Status Update", statusLabel + " for case " + trackingCode + ".");
        notifications.patientStatusUpdate(
                emergency.getPatient().getPhone(), emergency.getPatient().getEmail(),
                trackingCode, statusLabel);
        if (emergency.getDoctor() != null) {
            notifications.patientStatusUpdate(emergency.getDoctor().getPhone(), emergency.getDoctor().getEmail(),
                    trackingCode, statusLabel + " for case " + trackingCode);
        }

        return mapper.toEmergencyResponse(emergencyRepository.save(emergency));
    }

    public EmergencyCase findCase(String trackingCode) {
        return emergencyRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency case not found"));
    }

    private void history(EmergencyCase emergency, CaseStatus status, String note) {
        EmergencyStatusHistory history = new EmergencyStatusHistory();
        history.setEmergencyCase(emergency);
        history.setStatus(status);
        history.setNote(note);
        historyRepository.save(history);
    }
}
