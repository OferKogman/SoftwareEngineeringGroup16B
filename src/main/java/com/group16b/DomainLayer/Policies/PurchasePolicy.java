package com.group16b.DomainLayer.Policies;

public class PurchasePolicy {
    private int maxTicketsPerTransaction;
    private int minTicketsPerTransaction;
    private int minAge;
    private boolean noSingleSeatLeft;

    public PurchasePolicy(int maxTicketsPerTransaction, int minTicketsPerTransaction, int minAge, boolean noSingleSeatLeft){
        if (maxTicketsPerTransaction<=0){
            throw new IllegalArgumentException("Maximum ticket limit must be greater than 0.");
        }
        if(minTicketsPerTransaction<1){
            throw new IllegalArgumentException("Customer must buy at least 1 ticket.");
        }
        if(maxTicketsPerTransaction<minTicketsPerTransaction){
            throw new IllegalArgumentException("Maximum should be greater than or equal to minimum.");
        }
        if(minAge<0){
            throw new IllegalArgumentException("Minimum age cannot be negative.");
        }
        this.maxTicketsPerTransaction = maxTicketsPerTransaction;
        this.minTicketsPerTransaction = minTicketsPerTransaction;
        this.minAge = minAge;
        this.noSingleSeatLeft = noSingleSeatLeft;
    }

    public int getMaxTicketsPerTransaction(){
        return this.maxTicketsPerTransaction;
    }

    public int getMinTicketsPerTransaction(){
        return this.minTicketsPerTransaction;
    }

    public int getMinAge(){
        return this.minAge;
    }

    public boolean getNoSingleSeatLeft(){
        return this.noSingleSeatLeft;
    }

    public void setMaxTicketsPerTransaction(int maxTicketsPerTransaction){
        if (maxTicketsPerTransaction<=0){
            throw new IllegalArgumentException("Maximum ticket limit must be greater than 0.");
        }
        if(maxTicketsPerTransaction<this.minTicketsPerTransaction){
            throw new IllegalArgumentException("Maximum should be greater than or equal to minimum.");
        }
        this.maxTicketsPerTransaction = maxTicketsPerTransaction;
    }

    public void setMinTicketsPerTransaction(int minTicketsPerTransaction){
        if(minTicketsPerTransaction<1){
            throw new IllegalArgumentException("Customer must buy at least 1 ticket.");
        }
        if(this.maxTicketsPerTransaction<minTicketsPerTransaction){
            throw new IllegalArgumentException("Maximum should be greater than or equal to minimum.");
        }
        this.minTicketsPerTransaction = minTicketsPerTransaction;
    }

    public void setMinAge(int minAge){
        if(minAge<0){
            throw new IllegalArgumentException("Minimum age cannot be negative.");
        }
        this.minAge = minAge;
    }

    public void setNoSingleSeatLeft(boolean noSingleSeatLeft){
        this.noSingleSeatLeft = noSingleSeatLeft;
    }

    public boolean isAllowed(int age, int ticketCount, int seatsRemainingInSection){
        return (age >= this.minAge && ticketCount >= this.minTicketsPerTransaction && ticketCount <= this.maxTicketsPerTransaction && (!this.noSingleSeatLeft || seatsRemainingInSection - ticketCount != 1));
    }


}
