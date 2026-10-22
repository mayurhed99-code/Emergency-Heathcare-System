package com.emergency.platform.repository;

import com.emergency.platform.entity.Ambulance;
import com.emergency.platform.entity.AmbulanceStatus;
import com.emergency.platform.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    Optional<Ambulance> findFirstByStatus(AmbulanceStatus status);

    Optional<Ambulance> findFirstByHospitalAndStatus(Hospital hospital, AmbulanceStatus status);
}
