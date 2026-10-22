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
public class EmergencyCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingCode;

    @OneToOne(fetch = FetchType.LAZY)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private EmergencyType emergencyType;

    @Enumerated(EnumType.STRING)
    private SeverityLevel severity;

    @Enumerated(EnumType.STRING)
    private CaseStatus status = CaseStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    private Bed bed;

    @ManyToOne(fetch = FetchType.LAZY)
    private Ambulance ambulance;

    @ManyToOne(fetch = FetchType.LAZY)
    private Driver driver;

    private double simulatedHospitalDistanceKm;
    private double simulatedAmbulanceDistanceKm;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
