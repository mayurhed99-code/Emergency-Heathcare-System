package com.emergency.platform.service.admission;

import com.emergency.platform.dto.AdmissionCreateRequest;
import com.emergency.platform.dto.AdmissionResponse;
import com.emergency.platform.dto.AllocateBedRequest;
import com.emergency.platform.dto.AssignDoctorRequest;
import com.emergency.platform.entity.AdmissionStatus;
import com.emergency.platform.entity.Bed;
import com.emergency.platform.entity.BedStatus;
import com.emergency.platform.entity.Doctor;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.Patient;
import com.emergency.platform.entity.PlannedAdmission;
import com.emergency.platform.exception.ResourceNotFoundException;
import com.emergency.platform.exception.WorkflowException;
import com.emergency.platform.mapper.WorkflowMapper;
import com.emergency.platform.repository.BedRepository;
import com.emergency.platform.repository.DoctorRepository;
import com.emergency.platform.repository.HospitalRepository;
import com.emergency.platform.repository.PatientRepository;
import com.emergency.platform.repository.PlannedAdmissionRepository;
import com.emergency.platform.service.auth.OtpService;
import com.emergency.platform.service.notification.NotificationOrchestrator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AdmissionWorkflowService {
    private final PlannedAdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final OtpService otpService;
    private final NotificationOrchestrator notifications;
    private final WorkflowMapper mapper;

    public AdmissionWorkflowService(PlannedAdmissionRepository admissionRepository,
                                    PatientRepository patientRepository,
                                    HospitalRepository hospitalRepository,
                                    DoctorRepository doctorRepository,
                                    BedRepository bedRepository,
                                    OtpService otpService,
                                    NotificationOrchestrator notifications,
                                    WorkflowMapper mapper) {
        this.admissionRepository = admissionRepository;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.otpService = otpService;
        this.notifications = notifications;
        this.mapper = mapper;
    }

    public String startOtp(String target, String purpose) {
        return otpService.createOtp(target, purpose);
    }

    @Transactional
    public AdmissionResponse submit(AdmissionCreateRequest request) {
        String target = request.verificationMethod().name().equals("SMS") ? request.phone() : request.email();
        otpService.verify(target, "ADMISSION", request.otp());

        Patient patient = new Patient();
        patient.setName(request.name());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setLocationText(request.locationText());
        patientRepository.save(patient);

        Hospital hospital = hospitalRepository.findAll()
                .stream()
                .filter(item -> item.getSpecializations().contains(request.preferredSpecialization()))
                .filter(item -> item.getAvailableBeds() > 0)
                .min(Comparator.comparingInt(Hospital::getActiveCases))
                .orElseThrow(() -> new WorkflowException("No hospital is available for this admission"));

        PlannedAdmission admission = new PlannedAdmission();
        admission.setTrackingCode("ADM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        admission.setPatient(patient);
        admission.setSymptoms(request.symptoms());
        admission.setPreferredSpecialization(request.preferredSpecialization());
        admission.setVerificationMethod(request.verificationMethod());
        admission.setOtpVerified(true);
        admission.setStatus(AdmissionStatus.HOSPITAL_REVIEW);
        admission.setHospital(hospital);
        admissionRepository.save(admission);
        notifications.dashboard(hospital, com.emergency.platform.entity.Role.HOSPITAL, null, "Admission review", "Admission " + admission.getTrackingCode() + " requires review.", admission.getTrackingCode());
        return mapper.toAdmissionResponse(admission);
    }

    public List<AdmissionResponse> queue() {
        return admissionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PlannedAdmission::getCreatedAt).reversed())
                .map(mapper::toAdmissionResponse)
                .toList();
    }

    @Transactional
    public AdmissionResponse approve(String trackingCode) {
        PlannedAdmission admission = findAdmission(trackingCode);
        admission.setStatus(AdmissionStatus.APPROVED);
        notifications.admissionConfirmation(admission.getPatient().getPhone(), admission.getPatient().getEmail(), admission.getTrackingCode());
        return mapper.toAdmissionResponse(admissionRepository.save(admission));
    }

    @Transactional
    public AdmissionResponse assignDoctor(String trackingCode, AssignDoctorRequest request) {
        PlannedAdmission admission = findAdmission(trackingCode);
        Doctor doctor = request.doctorId() == null
                ? doctorRepository.findFirstByHospitalAndSpecializationAndAvailableTrue(admission.getHospital(), admission.getPreferredSpecialization())
                .orElseThrow(() -> new WorkflowException("No matching doctor available"))
                : doctorRepository.findById(request.doctorId()).orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        doctor.setAvailable(false);
        admission.setDoctor(doctor);
        admission.setStatus(AdmissionStatus.DOCTOR_ASSIGNED);
        doctorRepository.save(doctor);
        return mapper.toAdmissionResponse(admissionRepository.save(admission));
    }

    @Transactional
    public AdmissionResponse allocateBed(String trackingCode, AllocateBedRequest request) {
        PlannedAdmission admission = findAdmission(trackingCode);
        Bed bed = request.bedId() == null
                ? bedRepository.findFirstByHospitalAndStatus(admission.getHospital(), BedStatus.AVAILABLE)
                .orElseThrow(() -> new WorkflowException("No bed available"))
                : bedRepository.findById(request.bedId()).orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        bed.setStatus(BedStatus.RESERVED);
        admission.setBed(bed);
        admission.setStatus(AdmissionStatus.CONFIRMED);
        admission.getHospital().setAvailableBeds(Math.max(0, admission.getHospital().getAvailableBeds() - 1));
        bedRepository.save(bed);
        return mapper.toAdmissionResponse(admissionRepository.save(admission));
    }

    private PlannedAdmission findAdmission(String trackingCode) {
        return admissionRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Admission request not found"));
    }
}
