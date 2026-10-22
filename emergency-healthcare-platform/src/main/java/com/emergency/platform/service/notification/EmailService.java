package com.emergency.platform.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.host:}") String smtpHost,
                        @Value("${spring.mail.port:0}") int smtpPort,
                        @Value("${spring.mail.username:}") String smtpUsername,
                        @Value("${spring.mail.password:}") String smtpPassword) {
        this.mailSender = mailSender;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public boolean sendEmail(String to, String subject, String body) {
        try {
            if (to == null || to.isBlank()) {
                return false;
            }
            if (to.endsWith("@example.com") || to.endsWith("@ops.local")) {
                log.info("Email skipped for dummy seed data address: {}", to);
                return true;
            }
            if (smtpUsername == null || smtpUsername.isBlank() || smtpPassword == null || smtpPassword.isBlank()) {
                log.warn("Email skipped for {} because SMTP_USERNAME or SMTP_PASSWORD is not configured.", to);
                return false;
            }
            if (smtpHost != null && smtpHost.contains("gmail") && smtpPort == 1025) {
                log.warn("Email skipped for {} because smtp.gmail.com must use SMTP_PORT=587, not 1025.", to);
                return false;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);
            return true;
        } catch (Exception exception) {
            log.warn("Email delivery failed for {}: {}", to, exception.getMessage());
            return false;
        }
    }
}
