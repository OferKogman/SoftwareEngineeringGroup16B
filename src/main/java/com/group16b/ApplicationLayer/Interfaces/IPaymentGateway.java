package com.group16b.ApplicationLayer.Interfaces;

import com.group16b.ApplicationLayer.Records.PaymentInfo;

public interface IPaymentGateway {
    void processPayment(PaymentInfo paymentInfo, double price);
    void cancelPayment();

    
}
