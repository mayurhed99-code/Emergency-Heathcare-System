package com.emergency.platform.repository;

import com.emergency.platform.entity.CaseStatus;
import com.emergency.platform.entity.EmergencyCase;
import com.emergency.platform.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyCaseRepository extends JpaRepository<EmergencyCase, Long> {
    Optional<EmergencyCase> findByTrackingCode(String trackingCode);

    List<EmergencyCase> findByHospitalOrderByCreatedAtDesc(Hospital hospital);

    List<EmergencyCase> findByStatusInOrderByCreatedAtDesc(List<CaseStatus> statuses);
}
