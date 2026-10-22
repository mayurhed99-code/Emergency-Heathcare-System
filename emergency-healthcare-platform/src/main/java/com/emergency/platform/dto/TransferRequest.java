package com.emergency.platform.dto;

import com.emergency.platform.entity.TransferReason;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
        @NotNull TransferReason reason,
        String details,
        Long receivingHospitalId
) {
}
