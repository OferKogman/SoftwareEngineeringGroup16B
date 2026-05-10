package com.group16b.ApplicationLayer;

public class PaymentInfo {
    String cardNumber;
    String cardHolderName;
    String expirationDate;
    String cvv;
    public PaymentInfo(String cardNumber, String cardHolderName, String expirationDate, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
    }

    public String getCardNumber() {
        return cardNumber;
    }
    public String getCardHolderName() {
        return cardHolderName;
    }
    public String getExpirationDate() {
        return expirationDate;
    } 
    public String getCvv() {
        return cvv;
    }
    public boolean isvalid() {
        return true; //@TODO: implement actual validation logic
        //throw new UnsupportedOperationException("Payment validation not implemented yet");
    }
}
