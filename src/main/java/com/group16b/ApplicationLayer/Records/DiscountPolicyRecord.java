package com.group16b.ApplicationLayer.Records;

import java.time.LocalDateTime;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public record DiscountPolicyRecord(
        DiscountPolicyTypes type,
        Double percentage,

        Integer minAmount,
        Integer maxAmount,

        LocalDateTime startDate,
        LocalDateTime endDate,

        String code,
        LocalDateTime expirationDate,
        Integer maxUsages,

        DiscountPolicyRecord left,
        DiscountPolicyRecord right
) {
}