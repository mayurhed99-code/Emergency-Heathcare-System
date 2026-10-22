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
public class DashboardNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String referenceCode;

    @Enumerated(EnumType.STRING)
    private Role recipientRole;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser recipientUser;

    private boolean readFlag;
    private Instant createdAt = Instant.now();
}
