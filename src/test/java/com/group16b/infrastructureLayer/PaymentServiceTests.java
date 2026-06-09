package com.group16b.infrastructureLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.InfrastructureLayer.PaymentService;


class PaymentServiceTests{
    private RestTemplate restTemplate;
    private PaymentService paymentService;

    private final String TRANSACTION_ID_STRING="12345";
    private final int TRANSACTION_ID= Integer.valueOf(TRANSACTION_ID_STRING);

    
    @BeforeEach
    void setup()
    {
        restTemplate=mock(RestTemplate.class);
        paymentService=new PaymentService(restTemplate);
        ResponseEntity<String> response =new ResponseEntity<>(TRANSACTION_ID_STRING, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(response);
    }

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
    void givenValidPayment_whenProcessPayment_succes()
    {
        int id=paymentService.processPayment(validPayment(), 100);
        assertEquals(TRANSACTION_ID, id);
    }

    

}
