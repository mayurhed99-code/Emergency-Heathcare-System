package com.emergency.platform.repository;

import com.emergency.platform.entity.DashboardNotification;
import com.emergency.platform.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardNotificationRepository extends JpaRepository<DashboardNotification, Long> {
    List<DashboardNotification> findByHospitalOrderByCreatedAtDesc(Hospital hospital);
}
