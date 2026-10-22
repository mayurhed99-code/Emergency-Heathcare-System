package com.emergency.platform.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final String baseUrl;
    private final String traccarToken;
    private final String secretKey;
    private final String senderId;
    private final String mode;
    private final RestTemplate restTemplate = new RestTemplate();

    public SmsService(@Value("${app.sms.base-url}") String baseUrl,
                      @Value("${app.sms.traccar-token}") String traccarToken,
                      @Value("${app.sms.secret-key}") String secretKey,
                      @Value("${app.sms.sender-id}") String senderId,
                      @Value("${app.sms.mode:SIMULATION}") String mode) {
        this.baseUrl = baseUrl;
        this.traccarToken = traccarToken;
        this.secretKey = secretKey;
        this.senderId = senderId;
        this.mode = mode;
    }

    public boolean sendOtpSms(String phone, String otp) {
        return sendSms(phone, "Your emergency platform OTP is " + otp);
    }

    public boolean sendEmergencyAlertSms(String phone, String trackingCode) {
        return sendSms(phone, "Emergency case " + trackingCode + " requires immediate action.");
    }

    public boolean sendDoctorAssignmentSms(String phone, String trackingCode) {
        return sendSms(phone, "You are assigned to emergency case " + trackingCode + ".");
    }

    public boolean sendDriverAssignmentSms(String phone, String trackingCode) {
        return sendSms(phone, "Ambulance dispatch assigned for emergency case " + trackingCode + ".");
    }

    public boolean sendAdmissionConfirmationSms(String phone, String trackingCode) {
        return sendSms(phone, "Admission request " + trackingCode + " has been confirmed.");
    }

    private boolean sendSms(String phone, String message) {
        try {
            if (phone == null || phone.isBlank()) {
                log.warn("SMS skipped: phone is null or blank");
                return false;
            }
            if (phone.startsWith("+91990000") || phone.startsWith("+91980000") || phone.startsWith("+91970000")) {
                log.info("SMS skipped for dummy seed data phone: {}", phone);
                return true;
            }
            if (isSimulationMode()) {
                log.info("SMS SIMULATION to {} from {}: {}", phone, senderId, message);
                return true;
            }
            if (traccarToken.isBlank()) {
                throw new IllegalStateException("SMS credentials are incomplete");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", traccarToken);
            Map<String, String> payload = Map.of(
                    "to", phone,
                    "message", message
            );
            restTemplate.postForEntity(baseUrl, new HttpEntity<>(payload, headers), String.class);
            log.info("SMS sent to {} via provider {}", phone, baseUrl);
            return true;
        } catch (Exception exception) {
            log.warn("SMS delivery failed for {}: {}", phone, exception.getMessage());
            return false;
        }
    }

    private boolean isSimulationMode() {
        return !"REAL".equalsIgnoreCase(mode) || baseUrl == null || baseUrl.isBlank();
    }
}
