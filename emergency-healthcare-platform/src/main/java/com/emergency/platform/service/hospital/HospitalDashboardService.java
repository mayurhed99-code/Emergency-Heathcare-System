package com.emergency.platform.service.hospital;

import com.emergency.platform.dto.AdmissionResponse;
import com.emergency.platform.dto.EmergencyCaseResponse;
import com.emergency.platform.dto.HospitalDashboardResponse;
import com.emergency.platform.dto.TimelineItem;
import com.emergency.platform.entity.AmbulanceStatus;
import com.emergency.platform.entity.AppUser;
import com.emergency.platform.entity.BedStatus;
import com.emergency.platform.entity.CaseStatus;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.Role;
import com.emergency.platform.exception.ResourceNotFoundException;
import com.emergency.platform.mapper.WorkflowMapper;
import com.emergency.platform.repository.AmbulanceRepository;
import com.emergency.platform.repository.AppUserRepository;
import com.emergency.platform.repository.BedRepository;
import com.emergency.platform.repository.DashboardNotificationRepository;
import com.emergency.platform.repository.DoctorRepository;
import com.emergency.platform.repository.EmergencyCaseRepository;
import com.emergency.platform.repository.HospitalRepository;
import com.emergency.platform.repository.PatientTransferRepository;
import com.emergency.platform.repository.PlannedAdmissionRepository;
import com.emergency.platform.repository.DoctorRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class HospitalDashboardService {
    private final AppUserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final EmergencyCaseRepository emergencyCaseRepository;
    private final PlannedAdmissionRepository admissionRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final DashboardNotificationRepository notificationRepository;
    private final PatientTransferRepository transferRepository;
    private final DoctorRequestRepository doctorRequestRepository;
    private final WorkflowMapper mapper;

    public HospitalDashboardService(AppUserRepository userRepository,
                                    HospitalRepository hospitalRepository,
                                    EmergencyCaseRepository emergencyCaseRepository,
                                    PlannedAdmissionRepository admissionRepository,
                                    DoctorRepository doctorRepository,
                                    BedRepository bedRepository,
                                    AmbulanceRepository ambulanceRepository,
                                    DashboardNotificationRepository notificationRepository,
                                    PatientTransferRepository transferRepository,
                                    DoctorRequestRepository doctorRequestRepository,
                                    WorkflowMapper mapper) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.emergencyCaseRepository = emergencyCaseRepository;
        this.admissionRepository = admissionRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.notificationRepository = notificationRepository;
        this.transferRepository = transferRepository;
        this.doctorRequestRepository = doctorRequestRepository;
        this.mapper = mapper;
    }

    public HospitalDashboardResponse dashboard(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return dashboardForHospital(hospital);
    }

    public HospitalDashboardResponse dashboardForUser(String email, Long requestedHospitalId) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.HOSPITAL && user.getHospital() != null) {
            return dashboardForHospital(user.getHospital());
        }
        Long hospitalId = requestedHospitalId == null ? 1L : requestedHospitalId;
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return dashboardForHospital(hospital);
    }

    private HospitalDashboardResponse dashboardForHospital(Hospital hospital) {
        var emergencyQueue = emergencyCaseRepository.findByHospitalOrderByCreatedAtDesc(hospital)
                .stream()
                .map(mapper::toEmergencyResponse)
                .toList();
        var admissions = admissionRepository.findByHospitalOrderByCreatedAtDesc(hospital)
                .stream()
                .map(mapper::toAdmissionResponse)
                .toList();
        java.util.List<String> doctors = new java.util.ArrayList<>(doctorRepository.findByHospitalAndAvailableTrue(hospital)
                .stream()
                .map(doctor -> doctor.getName() + " - " + doctor.getSpecialization().name())
                .toList());
        doctorRequestRepository.findByHospitalAndStatus(hospital, "ACCEPTED")
                .stream()
                .filter(req -> req.getEmergencyCase().getStatus() == CaseStatus.DOCTOR_REQUESTED)
                .map(req -> req.getDoctor().getName() + " - " + req.getDoctor().getSpecialization().name() + " [EXTERNAL]")
                .forEach(doctors::add);
        var beds = bedRepository.findByHospitalAndStatus(hospital, BedStatus.AVAILABLE)
                .stream()
                .map(bed -> bed.getBedCode() + " - " + bed.getBedType().name())
                .toList();
        var ambulances = ambulanceRepository.findAll()
                .stream()
                .filter(ambulance -> ambulance.getHospital().getId().equals(hospital.getId()))
                .filter(ambulance -> ambulance.getStatus() == AmbulanceStatus.AVAILABLE)
                .map(ambulance -> ambulance.getCallSign() + " - " + ambulance.getDriver().getName())
                .toList();
        var notifications = notificationRepository.findByHospitalOrderByCreatedAtDesc(hospital)
                .stream()
                .map(item -> new TimelineItem(item.getTitle(), item.getMessage(), item.getCreatedAt()))
                .toList();
        int transfers = transferRepository.findByFromHospitalOrToHospitalOrderByCreatedAtDesc(hospital, hospital).size();
        
        long completedCases = emergencyQueue.stream()
                .filter(e -> e.status().equals(CaseStatus.DISCHARGED.name()))
                .count();
                
        long activeEmergencies = emergencyQueue.size() - completedCases;

        return new HospitalDashboardResponse(
                emergencyQueue,
                admissions,
                doctors,
                beds,
                ambulances,
                notifications,
                (int) activeEmergencies,
                admissions.size(),
                transfers,
                (int) completedCases
        );
    }
}
