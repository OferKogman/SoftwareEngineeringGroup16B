package com.group16b.ApplicationLayer.DTOs.PurchasePolicy;

import com.group16b.ApplicationLayer.Objects.PurchasePolicyTypes;

public class MinTicketsDTO extends PurchasePolicyDTO {
    private int minTickets;

    public MinTicketsDTO(int minTickets) {
        super(PurchasePolicyTypes.MIN_TICKETS);
        this.minTickets = minTickets;
    }

    public int getMinTickets() {
        return minTickets;
    }

}
