package com.emergency.platform.repository;

import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.PatientTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientTransferRepository extends JpaRepository<PatientTransfer, Long> {
    List<PatientTransfer> findByFromHospitalOrToHospitalOrderByCreatedAtDesc(Hospital fromHospital, Hospital toHospital);
}
