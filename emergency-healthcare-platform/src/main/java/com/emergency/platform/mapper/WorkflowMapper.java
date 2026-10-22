package com.emergency.platform.mapper;

import com.emergency.platform.dto.AdmissionResponse;
import com.emergency.platform.dto.EmergencyCaseResponse;
import com.emergency.platform.dto.TimelineItem;
import com.emergency.platform.entity.CaseStatus;
import com.emergency.platform.entity.EmergencyCase;
import com.emergency.platform.entity.EmergencyStatusHistory;
import com.emergency.platform.entity.NotificationDeliveryLog;
import com.emergency.platform.entity.PlannedAdmission;
import com.emergency.platform.repository.EmergencyStatusHistoryRepository;
import com.emergency.platform.repository.NotificationDeliveryLogRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class WorkflowMapper {
    private final NotificationDeliveryLogRepository deliveryLogRepository;
    private final EmergencyStatusHistoryRepository historyRepository;

    public WorkflowMapper(NotificationDeliveryLogRepository deliveryLogRepository,
                          EmergencyStatusHistoryRepository historyRepository) {
        this.deliveryLogRepository = deliveryLogRepository;
        this.historyRepository = historyRepository;
    }

    public EmergencyCaseResponse toEmergencyResponse(EmergencyCase emergencyCase) {
        List<TimelineItem> workflowTimeline = historyRepository.findByEmergencyCaseOrderByCreatedAtAsc(emergencyCase)
                .stream()
                .map(this::toTimeline)
                .toList();
        List<TimelineItem> notificationTimeline = deliveryLogRepository.findByReferenceCodeOrderByCreatedAtAsc(emergencyCase.getTrackingCode())
                .stream()
                .map(this::toTimeline)
                .toList();
        List<TimelineItem> timeline = Stream.concat(workflowTimeline.stream(), notificationTimeline.stream())
                .sorted(Comparator.comparing(TimelineItem::createdAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        return new EmergencyCaseResponse(
                emergencyCase.getTrackingCode(),
                emergencyCase.getPatient().getName(),
                emergencyCase.getPatient().getPhone(),
                emergencyCase.getEmergencyType().name(),
                emergencyCase.getSeverity().name(),
                emergencyCase.getStatus().name(),
                emergencyCase.getHospital() == null ? null : emergencyCase.getHospital().getName(),
                emergencyCase.getAmbulance() == null ? null : emergencyCase.getAmbulance().getCallSign(),
                emergencyCase.getDriver() == null ? null : emergencyCase.getDriver().getName(),
                emergencyCase.getDoctor() == null ? null : emergencyCase.getDoctor().getName(),
                emergencyCase.getBed() == null ? null : emergencyCase.getBed().getBedCode(),
                emergencyCase.getSimulatedHospitalDistanceKm(),
                emergencyCase.getSimulatedAmbulanceDistanceKm(),
                timeline
        );
    }

    public AdmissionResponse toAdmissionResponse(PlannedAdmission admission) {
        return new AdmissionResponse(
                admission.getTrackingCode(),
                admission.getPatient().getName(),
                admission.getStatus().name(),
                admission.getHospital() == null ? null : admission.getHospital().getName(),
                admission.getDoctor() == null ? null : admission.getDoctor().getName(),
                admission.getBed() == null ? null : admission.getBed().getBedCode(),
                "PDF confirmation will be generated after bed assignment"
        );
    }

    private TimelineItem toTimeline(NotificationDeliveryLog log) {
        return new TimelineItem(
                notificationLabel(log.getEventType()),
                log.getChannel().name() + " - " + log.getStatus().name() + " - " + log.getDetail(),
                log.getCreatedAt()
        );
    }

    private TimelineItem toTimeline(EmergencyStatusHistory history) {
        return new TimelineItem(
                workflowLabel(history.getStatus()),
                history.getNote(),
                history.getCreatedAt()
        );
    }

    private String workflowLabel(CaseStatus status) {
        return switch (status) {
            case CREATED -> "Emergency Created";
            case HOSPITAL_ALERT_SENT -> "Hospital Assigned";
            case AMBULANCE_ASSIGNED -> "Ambulance Assigned";
            case DRIVER_ALERT_SENT -> "Driver Alert Sent";
            case ACCEPTED -> "Emergency Accepted";
            case DOCTOR_REQUESTED -> "External Specialist Requested";
            case DOCTOR_ALERT_SENT, DOCTOR_ASSIGNED -> "Doctor Assigned";
            case BED_ALLOCATED -> "Bed Allocated";
            case UNDER_TREATMENT -> "Treatment Started";
            case CRITICAL -> "Marked Critical";
            case STABLE -> "Marked Stable";
            case DISCHARGED -> "Discharged";
            case TRANSFER_REQUESTED -> "Transfer Requested";
            case TRANSFER_ACCEPTED -> "Transfer Accepted";
            case ASSIGNED -> "Case Assigned";
        };
    }

    private String notificationLabel(String eventType) {
        if ("Hospital Alert Sent".equals(eventType)) {
            return "Hospital Notified";
        }
        return eventType;
    }
}
