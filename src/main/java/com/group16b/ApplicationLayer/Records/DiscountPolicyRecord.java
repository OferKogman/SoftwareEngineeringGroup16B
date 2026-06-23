package com.group16b.ApplicationLayer.Records;

import java.time.LocalDateTime;
import com.group16b.ApplicationLayer.Objects.DiscountPolicyTypes;

public record DiscountPolicyRecord(
        DiscountPolicyTypes type,
        Double discountPercentage,
        Integer minTickets,
        Integer maxTickets,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String couponCode,
        LocalDateTime expiryDate,
        Integer maxUsages,
        DiscountPolicyRecord left,
        DiscountPolicyRecord right
) {}