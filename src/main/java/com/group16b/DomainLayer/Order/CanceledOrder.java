package com.group16b.DomainLayer.Order;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CanceledOrder implements OrderState {

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public OrderState copy() {
        return new CanceledOrder();
    }

    @Override
    public List<String> getTickets() {
        throw new IllegalStateException("Cannot get tickets from a canceled order");
    }

    @Override
    public void setTickets(List<String> tickets) {
        throw new IllegalStateException("Cannot set tickets for a canceled order");
    }

    @Override
    public CompletedOrder completeOrder() {
        throw new IllegalStateException("Cannot complete a canceled order");
    }
    
}
