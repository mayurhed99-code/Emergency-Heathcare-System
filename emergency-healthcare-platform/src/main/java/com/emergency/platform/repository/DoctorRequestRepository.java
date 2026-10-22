package com.emergency.platform.repository;

import com.emergency.platform.entity.DoctorRequest;
import com.emergency.platform.entity.EmergencyCase;
import com.emergency.platform.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRequestRepository extends JpaRepository<DoctorRequest, Long> {
    List<DoctorRequest> findByHospitalAndStatus(Hospital hospital, String status);
    Optional<DoctorRequest> findFirstByEmergencyCaseAndStatus(EmergencyCase emergencyCase, String status);
}
