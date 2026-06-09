package com.group16b.InfrastructureLayer;

import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Records.PaymentInfo;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService implements IPaymentGateway {

    private static final String BASE_URL="https://damp-lynna-wsep-1984852e.koyeb.app/";

    private final RestTemplate restTemplate;

    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //pay, returns the transaction id if successful, or -1 on fail
    public int processPayment(PaymentInfo paymentInfo, double price)
    {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("action_type","pay");
        requestBody.add("currency", paymentInfo.currency());
        requestBody.add("cardNumber", paymentInfo.cardNumber());
        requestBody.add("month", String.valueOf(paymentInfo.month()));
        requestBody.add("year", String.valueOf(paymentInfo.year()));
        requestBody.add("holder", paymentInfo.holder());
        requestBody.add("cvv", paymentInfo.cvv());
        requestBody.add("id", paymentInfo.id());
        requestBody.add("price", String.valueOf(price));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, requestEntity, String.class);

        return Integer.parseInt(response.getBody().trim());
    }

    public void cancelPayment(){}

}
