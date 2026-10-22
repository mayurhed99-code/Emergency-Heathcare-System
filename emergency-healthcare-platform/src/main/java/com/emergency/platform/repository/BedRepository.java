package com.emergency.platform.repository;

import com.emergency.platform.entity.Bed;
import com.emergency.platform.entity.BedStatus;
import com.emergency.platform.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByHospitalAndStatus(Hospital hospital, BedStatus status);

    Optional<Bed> findFirstByHospitalAndStatus(Hospital hospital, BedStatus status);
}
