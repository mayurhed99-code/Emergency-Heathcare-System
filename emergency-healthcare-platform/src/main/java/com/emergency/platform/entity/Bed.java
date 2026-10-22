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

@Entity
@Getter
@Setter
public class Bed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bedCode;

    @Enumerated(EnumType.STRING)
    private BedType bedType;

    @Enumerated(EnumType.STRING)
    private BedStatus status = BedStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hospital hospital;
}
