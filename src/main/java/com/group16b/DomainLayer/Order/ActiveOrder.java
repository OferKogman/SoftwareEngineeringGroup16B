package com.group16b.DomainLayer.Order;

import java.util.List;
import java.util.Timer;

class ActiveOrder implements OrderState {
    private final Timer OrderTimer;
    private static final int ORDER_TIMEOUT = 10 * 60 * 1000; // 10 minutes

    protected ActiveOrder() {
        this.OrderTimer = new Timer(); // in each function. make sure timer is less then 10 minutes. if more then 10 minutes, cancel order and release seats
    }

    @Override
    public List<String> getTickets() {

        throw new IllegalStateException("Cannot get tickets from an active order");
        //return null; // can only get tickets from completed order
    }

    @Override
    public CompletedOrder completeOrder() {
        List<String> tickets = List.of(); // Generate tickets based on seats
        return new CompletedOrder(tickets);
    }

}
