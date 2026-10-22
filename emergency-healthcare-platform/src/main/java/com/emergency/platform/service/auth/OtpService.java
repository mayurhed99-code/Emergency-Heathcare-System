package com.emergency.platform.service.auth;

import com.emergency.platform.entity.OtpVerification;
import com.emergency.platform.exception.WorkflowException;
import com.emergency.platform.repository.OtpVerificationRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class OtpService {
    private final OtpVerificationRepository otpRepository;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpVerificationRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String createOtp(String target, String purpose) {
        String code = String.valueOf(100000 + random.nextInt(900000));
        OtpVerification otp = new OtpVerification();
        otp.setTarget(target);
        otp.setPurpose(purpose);
        otp.setCode(code);
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        otpRepository.save(otp);
        return code;
    }

    public void verify(String target, String purpose, String code) {
        OtpVerification otp = otpRepository.findFirstByTargetAndPurposeAndConsumedFalseOrderByExpiresAtDesc(target, purpose)
                .orElseThrow(() -> new WorkflowException("OTP not found or already used"));
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new WorkflowException("OTP expired");
        }
        if (!otp.getCode().equals(code)) {
            throw new WorkflowException("Invalid OTP");
        }
        otp.setConsumed(true);
        otpRepository.save(otp);
    }
}
