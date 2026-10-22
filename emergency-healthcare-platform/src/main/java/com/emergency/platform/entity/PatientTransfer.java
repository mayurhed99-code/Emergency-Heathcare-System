package com.emergency.platform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class PatientTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private EmergencyCase emergencyCase;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hospital fromHospital;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hospital toHospital;

    @Enumerated(EnumType.STRING)
    private TransferReason reason;

    private String details;

    @Enumerated(EnumType.STRING)
    private TransferStatus status = TransferStatus.CREATED;

    private Instant createdAt = Instant.now();
}
