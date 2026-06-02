package com.group16b.ApplicationLayer.DTOs;

import com.group16b.ApplicationLayer.Records.PaymentInfo;

public class CompleteActiveOrderRequestDTO {
    private String userId, orderID;
    private PaymentInfo paymentInfo;  

    public CompleteActiveOrderRequestDTO(String userId, String orderID, PaymentInfo paymentInfo){
        this.userId = userId;
        this.orderID = orderID;
        this.paymentInfo = paymentInfo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }  

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    
}
