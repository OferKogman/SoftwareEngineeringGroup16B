package com.group16b.ApplicationLayer.Records;

import com.group16b.ApplicationLayer.Objects.PurchasePolicyTypes;

public record PurchasePolicyRecord(PurchasePolicyTypes type, Integer minAge, Integer maxAge, Integer minTickets,
        Integer maxTickets, PurchasePolicyRecord left, PurchasePolicyRecord right) {
}