package com.group16b.ApplicationLayer.DTOs.PurchasePolicy;

import com.group16b.ApplicationLayer.Objects.PurchasePolicyTypes;

public class MaxTicketsDTO extends PurchasePolicyDTO {
    private int maxTickets;

    public MaxTicketsDTO(int maxTickets) {
        super(PurchasePolicyTypes.MAX_TICKETS);
        this.maxTickets = maxTickets;
    }

    public int getMaxTickets() {
        return maxTickets;
    }

}
