package com.group16b.ApplicationLayer.Interfaces;

import com.group16b.ApplicationLayer.Records.PaymentInfo;

public interface IPaymentGateway {
    int processPayment(PaymentInfo paymentInfo, double amount);
    void cancelPayment(int transactionId);

    
}
