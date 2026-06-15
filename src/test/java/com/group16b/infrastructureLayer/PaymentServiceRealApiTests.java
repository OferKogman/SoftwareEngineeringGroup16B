package com.group16b.infrastructureLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.InfrastructureLayer.PaymentService;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;


@Tag("external")
class PaymentServiceRealApiTests {

    private PaymentService paymentService=new PaymentService(new WsepClient(new RestTemplate()));

    private PaymentInfo validPayment() {
        return new PaymentInfo(
                "USD",
                "2222333344445555",
                12,
                2026,
                "Test User",
                "123",
                "20444444"
        );
    }

    @Test
    void RefundHitsProvider() {
        assertDoesNotThrow(() -> {
            paymentService.processPayment(validPayment(), 10.0);
        });
    }

    @Test
    void PaymentWithValidDataDoesntThrowAndTransIDIsCorrect()
    {
        int id=paymentService.processPayment(validPayment(), 10.0);
        assertNotEquals(-1,id);
        assertTrue(id > 10000 && id < 100000);
    }


}