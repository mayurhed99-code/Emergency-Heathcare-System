package com.emergency.platform.repository;

import com.emergency.platform.entity.EmergencyCase;
import com.emergency.platform.entity.EmergencyStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyStatusHistoryRepository extends JpaRepository<EmergencyStatusHistory, Long> {
    List<EmergencyStatusHistory> findByEmergencyCaseOrderByCreatedAtAsc(EmergencyCase emergencyCase);
}
