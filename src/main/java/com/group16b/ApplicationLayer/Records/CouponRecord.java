package com.group16b.ApplicationLayer.Records;

import java.time.LocalDateTime;

public record CouponRecord(
        String code,
        Double percentage,
        LocalDateTime expirationDate,
        Integer maxUses) {
}