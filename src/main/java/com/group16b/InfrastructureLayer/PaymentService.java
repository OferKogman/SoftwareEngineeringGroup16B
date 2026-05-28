package com.group16b.InfrastructureLayer;

import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import org.springframework.stereotype.Service;

@Service
public class PaymentService implements IPaymentGateway {
    public void processPayment(PaymentInfo paymentInfo, double price){
        if (paymentInfo == null || price <= 0) {
            throw new PaymentFailedException("Payment processing failed for payment info: " + paymentInfo + " with price: " + price);
        }
    }

    @Override
    public void cancelPayment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelPayment'");
    }

}
