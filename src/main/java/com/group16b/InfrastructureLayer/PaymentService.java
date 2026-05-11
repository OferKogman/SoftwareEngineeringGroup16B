package com.group16b.InfrastructureLayer;

import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;

public class PaymentService implements IPaymentGateway {
    public boolean processPayment(PaymentInfo paymentInfo, double price){return true;}

}
