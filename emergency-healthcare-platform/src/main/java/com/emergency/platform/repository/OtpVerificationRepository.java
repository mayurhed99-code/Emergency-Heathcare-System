package com.emergency.platform.repository;

import com.emergency.platform.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findFirstByTargetAndPurposeAndConsumedFalseOrderByExpiresAtDesc(String target, String purpose);
}
