package com.emergency.platform.service.hospital;

import com.emergency.platform.dto.ResourceDto.*;
import com.emergency.platform.entity.*;
import com.emergency.platform.exception.ResourceNotFoundException;
import com.emergency.platform.exception.WorkflowException;
import com.emergency.platform.repository.AmbulanceRepository;
import com.emergency.platform.repository.AppUserRepository;
import com.emergency.platform.repository.DoctorRepository;
import com.emergency.platform.repository.DriverRepository;
import com.emergency.platform.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalResourceService {
    private final DoctorRepository doctorRepository;
    private final DriverRepository driverRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final AppUserRepository userRepository;
    private final HospitalRepository hospitalRepository;

    public HospitalResourceService(DoctorRepository doctorRepository,
                                   DriverRepository driverRepository,
                                   AmbulanceRepository ambulanceRepository,
                                   AppUserRepository userRepository,
                                   HospitalRepository hospitalRepository) {
        this.doctorRepository = doctorRepository;
        this.driverRepository = driverRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
    }

    private Hospital getHospitalForUser(String email) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.HOSPITAL || user.getHospital() == null) {
            throw new WorkflowException("Only hospital staff can perform this action");
        }
        return user.getHospital();
    }

    // --- DOCTORS ---
    public List<DoctorDto> getDoctors(String email) {
        Hospital hospital = getHospitalForUser(email);
        return doctorRepository.findByHospitalAndAvailableTrue(hospital).stream()
                .map(d -> new DoctorDto(d.getId(), d.getName(), d.getEmail(), d.getPhone(), d.getSpecialization(), d.isAvailable()))
                .collect(Collectors.toList());
    }

    public DoctorDto addDoctor(String email, CreateDoctorRequest request) {
        Hospital hospital = getHospitalForUser(email);
        Doctor doc = new Doctor();
        doc.setName(request.name());
        doc.setEmail(request.email());
        doc.setPhone(request.phone());
        doc.setSpecialization(request.specialization());
        doc.setHospital(hospital);
        doc.setAvailable(true);
        doc = doctorRepository.save(doc);
        return new DoctorDto(doc.getId(), doc.getName(), doc.getEmail(), doc.getPhone(), doc.getSpecialization(), doc.isAvailable());
    }

    // --- DRIVERS ---
    public List<DriverDto> getDrivers(String email) {
        Hospital hospital = getHospitalForUser(email);
        // Drivers are currently tied to ambulances which are tied to hospitals.
        return ambulanceRepository.findAll().stream()
                .filter(a -> a.getHospital().getId().equals(hospital.getId()) && a.getDriver() != null)
                .map(Ambulance::getDriver)
                .map(d -> new DriverDto(d.getId(), d.getName(), d.getEmail(), d.getPhone(), d.getLicenseNumber(), d.isAvailable(), d.getCompletedTrips()))
                .collect(Collectors.toList());
    }

    public DriverDto addDriver(String email, CreateDriverRequest request) {
        Hospital hospital = getHospitalForUser(email);
        Driver driver = new Driver();
        driver.setName(request.name());
        driver.setEmail(request.email());
        driver.setPhone(request.phone());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setAvailable(true);
        driver.setCompletedTrips(0);
        driver = driverRepository.save(driver);
        return new DriverDto(driver.getId(), driver.getName(), driver.getEmail(), driver.getPhone(), driver.getLicenseNumber(), driver.isAvailable(), driver.getCompletedTrips());
    }

    // --- AMBULANCES ---
    public List<AmbulanceDto> getAmbulances(String email) {
        Hospital hospital = getHospitalForUser(email);
        return ambulanceRepository.findAll().stream()
                .filter(a -> a.getHospital().getId().equals(hospital.getId()))
                .map(a -> {
                    DriverDto driverDto = a.getDriver() != null ? new DriverDto(a.getDriver().getId(), a.getDriver().getName(), a.getDriver().getEmail(), a.getDriver().getPhone(), a.getDriver().getLicenseNumber(), a.getDriver().isAvailable(), a.getDriver().getCompletedTrips()) : null;
                    return new AmbulanceDto(a.getId(), a.getCallSign(), a.getStatus(), driverDto);
                })
                .collect(Collectors.toList());
    }

    public AmbulanceDto addAmbulance(String email, CreateAmbulanceRequest request) {
        Hospital hospital = getHospitalForUser(email);
        Ambulance amb = new Ambulance();
        amb.setCallSign(request.callSign());
        amb.setStatus(AmbulanceStatus.AVAILABLE);
        amb.setHospital(hospital);
        amb = ambulanceRepository.save(amb);
        return new AmbulanceDto(amb.getId(), amb.getCallSign(), amb.getStatus(), null);
    }
}
