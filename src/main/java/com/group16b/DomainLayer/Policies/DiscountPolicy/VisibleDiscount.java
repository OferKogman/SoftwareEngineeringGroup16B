package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
public abstract class VisibleDiscount extends Discount {
    private LocalDateTime expiryDate;
    private List<String> applicableTicketTypes; //TODO, REPLACE WITH ACTUAL TICKETS IF POSSIBLE, CONSULT TEAM/OFER FOR CONTEXT
    protected VisibleDiscount(double discountPercentage, double discountAmount, LocalDateTime expiryDate) {
        super(discountPercentage, discountAmount);
        if(expiryDate == null) {this.expiryDate = LocalDateTime.MAX;}
        else {
            if (LocalDateTime.now().isAfter(expiryDate)) {
                throw new IllegalArgumentException("discount expired");
            }
            this.expiryDate = expiryDate;
        }
        this.applicableTicketTypes = new ArrayList<>();
    }

    public LocalDateTime getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        if(expiryDate == null) {this.expiryDate = LocalDateTime.MAX; return;}
        if(LocalDateTime.now().isAfter(expiryDate)){
            throw new IllegalArgumentException("discount expired");
        }
        this.expiryDate = expiryDate;
    }

    public List<String> getApplicableTicketTypes() {
        return this.applicableTicketTypes;
    }

    public void setApplicableTicketTypes(List<String> applicableTicketTypes) {
        this.applicableTicketTypes = applicableTicketTypes;
    }

}
