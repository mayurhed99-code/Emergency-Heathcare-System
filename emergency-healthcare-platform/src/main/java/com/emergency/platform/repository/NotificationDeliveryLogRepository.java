package com.emergency.platform.repository;

import com.emergency.platform.entity.NotificationDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {
    List<NotificationDeliveryLog> findByReferenceCodeOrderByCreatedAtAsc(String referenceCode);
}
