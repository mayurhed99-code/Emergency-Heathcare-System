package com.emergency.platform.repository;

import com.emergency.platform.entity.Doctor;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalAndAvailableTrue(Hospital hospital);

    Optional<Doctor> findFirstByHospitalAndSpecializationAndAvailableTrue(Hospital hospital, Specialization specialization);

    Optional<Doctor> findByEmail(String email);
}
