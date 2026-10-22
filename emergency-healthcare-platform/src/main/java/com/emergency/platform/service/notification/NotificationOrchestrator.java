package com.emergency.platform.service.notification;

import com.emergency.platform.entity.AppUser;
import com.emergency.platform.entity.DashboardNotification;
import com.emergency.platform.entity.Hospital;
import com.emergency.platform.entity.NotificationChannel;
import com.emergency.platform.entity.NotificationDeliveryLog;
import com.emergency.platform.entity.NotificationStatus;
import com.emergency.platform.entity.Role;
import com.emergency.platform.repository.DashboardNotificationRepository;
import com.emergency.platform.repository.NotificationDeliveryLogRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationOrchestrator {
    private final DashboardNotificationRepository dashboardRepository;
    private final NotificationDeliveryLogRepository deliveryLogRepository;
    private final SmsService smsService;
    private final EmailService emailService;

    public NotificationOrchestrator(DashboardNotificationRepository dashboardRepository,
                                    NotificationDeliveryLogRepository deliveryLogRepository,
                                    SmsService smsService,
                                    EmailService emailService) {
        this.dashboardRepository = dashboardRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    public void dashboard(Hospital hospital, Role role, AppUser user, String title, String message, String referenceCode) {
        DashboardNotification notification = new DashboardNotification();
        notification.setHospital(hospital);
        notification.setRecipientRole(role);
        notification.setRecipientUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceCode(referenceCode);
        dashboardRepository.save(notification);
        log(referenceCode, "Dashboard Notification", NotificationChannel.DASHBOARD, role.name(), NotificationStatus.SENT, title);
    }

    public void hospitalEmergencyAlert(Hospital hospital, String trackingCode) {
        dashboard(hospital, Role.HOSPITAL, null, "Emergency incoming", "Case " + trackingCode + " assigned to your hospital.", trackingCode);
        sendSmsAndLog(trackingCode, "Hospital Alert Sent", hospital.getContactPhone(), hospital.getContactEmail());
        sendEmailAndLog(trackingCode, "Emergency " + trackingCode, "New emergency case assigned to your hospital. Immediate action required.", hospital.getContactEmail());
    }

    public void driverAssignment(String phone, String email, String trackingCode) {
        sendSmsAndLog(trackingCode, "Driver Alert Sent", phone, email);
        sendEmailAndLog(trackingCode, "Driver dispatch " + trackingCode, "Ambulance dispatch assigned. Please respond immediately.", email);
    }

    public void patientEmergencyConfirmation(String phone, String email, String trackingCode, String hospitalName) {
        String message = "Emergency " + trackingCode + " received. Assigned hospital: " + hospitalName + ". Help is on the way.";
        sendSmsAndLog(trackingCode, "Patient Confirmation Sent", phone, email);
        sendEmailAndLog(trackingCode, "Emergency request " + trackingCode, message, email);
    }

    public void doctorAssignment(String phone, String email, String trackingCode) {
        sendSmsAndLog(trackingCode, "Doctor Alert Sent", phone, email);
        sendEmailAndLog(trackingCode, "Doctor assignment " + trackingCode, "Emergency case assigned to you. Please review immediately.", email);
    }

    public void admissionConfirmation(String phone, String email, String trackingCode) {
        sendSmsAndLog(trackingCode, "Admission Approved", phone, email);
        sendEmailAndLog(trackingCode, "Admission confirmation " + trackingCode, "Your planned admission is confirmed.", email);
    }

    /**
     * Notify the patient about a status change in their emergency case.
     * Called at every workflow step so the patient stays informed.
     */
    public void patientStatusUpdate(String phone, String email, String trackingCode, String statusMessage) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            String smsMsg = "Case " + trackingCode + " update: " + statusMessage;
            boolean smsSent = smsService.sendEmergencyAlertSms(phone, trackingCode + " - " + statusMessage);
            log(trackingCode, "Patient Status Update", NotificationChannel.SMS, phone, status(smsSent), smsMsg);
            if (!smsSent && email != null && !email.isBlank()) {
                emailService.sendEmail(email, "Case update " + trackingCode, smsMsg);
            }
            sendEmailAndLog(trackingCode, "Case update " + trackingCode, smsMsg, email);
        });
    }

    /**
     * Notify hospital dashboard about a workflow step change.
     */
    public void hospitalStepNotification(Hospital hospital, String trackingCode, String title, String message) {
        dashboard(hospital, Role.HOSPITAL, null, title, message, trackingCode);
    }

    public void custom(Hospital hospital, String referenceCode, String title, String message, String recipient) {
        if (hospital != null) {
            dashboard(hospital, Role.HOSPITAL, null, title, message, referenceCode);
        }
        sendEmailAndLog(referenceCode, title, message, recipient);
    }

    private void sendSmsAndLog(String referenceCode, String eventType, String phone, String fallbackEmail) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            if (phone == null || phone.isBlank()) {
                log(referenceCode, eventType, NotificationChannel.SMS, "N/A", NotificationStatus.FAILED, "No phone number");
                return;
            }
            boolean smsSent = smsService.sendEmergencyAlertSms(phone, referenceCode);
            log(referenceCode, eventType, NotificationChannel.SMS, phone, status(smsSent), smsSent ? "SMS delivered" : "SMS failed");
            if (!smsSent && fallbackEmail != null && !fallbackEmail.isBlank()) {
                boolean fallbackSent = emailService.sendEmail(fallbackEmail, eventType + " (SMS fallback)", "SMS delivery failed. Original alert for case " + referenceCode + ".");
                log(referenceCode, "Email Fallback", NotificationChannel.EMAIL, fallbackEmail, fallbackSent ? NotificationStatus.FALLBACK_SENT : NotificationStatus.FAILED, eventType);
            }
        });
    }

    private void sendEmailAndLog(String referenceCode, String subject, String body, String email) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            if (email == null || email.isBlank()) {
                return;
            }
            boolean emailSent = emailService.sendEmail(email, subject, body);
            log(referenceCode, "Email Sent", NotificationChannel.EMAIL, email, status(emailSent), subject);
        });
    }

    private NotificationStatus status(boolean sent) {
        return sent ? NotificationStatus.SENT : NotificationStatus.FAILED;
    }

    private void log(String referenceCode, String eventType, NotificationChannel channel, String recipient, NotificationStatus status, String detail) {
        NotificationDeliveryLog entry = new NotificationDeliveryLog();
        entry.setReferenceCode(referenceCode);
        entry.setEventType(eventType);
        entry.setChannel(channel);
        entry.setRecipient(recipient);
        entry.setStatus(status);
        entry.setDetail(detail);
        deliveryLogRepository.save(entry);
    }
}
