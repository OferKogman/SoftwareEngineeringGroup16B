package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class BuyXGetYDiscount implements DiscountPolicy {
    private int x;
    private int y;
    private int ticketCount;
    public BuyXGetYDiscount(int x, int y, int ticketCount) {
        if(x<1){
            throw new IllegalArgumentException("You can't buy less than 1 ticket");
        }
        if(y<1){
            throw new IllegalArgumentException("You don't get less than 1 ticket");
        }
        if(ticketCount<1){
            throw new IllegalArgumentException("You can't buy less than 1 ticket");
        }
        this.x = x;
        this.y = y;
        this.ticketCount = ticketCount;
    }
    public double calculateDiscount(double basePrice){ //if you bought (for any int n >=0 and for any m>=0 and m<=x) n*(x+y) +m tickets, you will pay for n*x + m tickets
        double pricePerTicket = basePrice/ticketCount;
        if(x>=ticketCount) {return basePrice;} //didn't buy more than x, no discount to be applied
        if(x+y>=ticketCount) {return x*pricePerTicket;} //bought more than x but not more than x+y, only pay for x
        int w = ticketCount/(x+y); //guaranteed >=1 now since ticketCount > x+y
        int z = ticketCount % (x+y); //ticketCount = w*(x+y)+z, x+y>z due to modulo
        if(z>x){ //explanation in docs
            z=x;
        }
        return (w*x+z)*pricePerTicket;
    }
}
