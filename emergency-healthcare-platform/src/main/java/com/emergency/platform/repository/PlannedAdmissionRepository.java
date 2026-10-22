package com.emergency.platform.repository;

import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.PlannedAdmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlannedAdmissionRepository extends JpaRepository<PlannedAdmission, Long> {
    Optional<PlannedAdmission> findByTrackingCode(String trackingCode);

    List<PlannedAdmission> findByHospitalOrderByCreatedAtDesc(Hospital hospital);
}
