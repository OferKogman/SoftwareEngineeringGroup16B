package com.group16b.ApplicationLayer.Records;

import java.time.LocalDateTime;
import java.util.List;

public record DiscountPolicyRecord(
        String type,
        Double discountPercentage,
        Integer minTickets,
        Integer maxTickets,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String couponCode,
        LocalDateTime expiryDate,
        Integer maxUsages,
        List<DiscountPolicyRecord> children
) {}