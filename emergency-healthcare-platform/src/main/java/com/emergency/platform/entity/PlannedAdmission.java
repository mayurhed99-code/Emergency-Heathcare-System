package com.emergency.platform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class PlannedAdmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingCode;

    @OneToOne(fetch = FetchType.LAZY)
    private Patient patient;

    private String symptoms;

    @Enumerated(EnumType.STRING)
    private Specialization preferredSpecialization;

    @Enumerated(EnumType.STRING)
    private VerificationMethod verificationMethod;

    @Enumerated(EnumType.STRING)
    private AdmissionStatus status = AdmissionStatus.SUBMITTED;

    private boolean otpVerified;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    private Bed bed;

    private Instant createdAt = Instant.now();
}
