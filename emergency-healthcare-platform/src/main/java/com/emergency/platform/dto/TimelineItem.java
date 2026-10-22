package com.emergency.platform.dto;

import java.time.Instant;

public record TimelineItem(
        String label,
        String detail,
        Instant createdAt
) {
}
