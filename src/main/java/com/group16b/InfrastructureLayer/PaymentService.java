package com.group16b.InfrastructureLayer;

import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Exceptions.RefundFailedException;
import com.group16b.ApplicationLayer.Exceptions.RefundStatusUnknownException;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Records.PaymentInfo;

import java.math.BigDecimal;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService implements IPaymentGateway {

    private static final String BASE_URL="https://damp-lynna-wsep-1984852e.koyeb.app/";

    private final RestTemplate restTemplate;

    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //pay, returns the transaction id if successful, or throw on failure
    public int processPayment(PaymentInfo paymentInfo, double amount)
    {   
        //prepare the http request
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("action_type","pay");
        requestBody.add("currency", paymentInfo.currency());
        requestBody.add("card_number", paymentInfo.cardNumber());
        requestBody.add("month", String.valueOf(paymentInfo.month()));
        requestBody.add("year", String.valueOf(paymentInfo.year()));
        requestBody.add("holder", paymentInfo.holder());
        requestBody.add("cvv", paymentInfo.cvv());
        requestBody.add("id", paymentInfo.id());
        requestBody.add("amount", BigDecimal.valueOf(amount).toPlainString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        //send and get the response
        final ResponseEntity<String> response;

        try{
            response = restTemplate.postForEntity(BASE_URL, requestEntity, String.class);
        }catch (RestClientException  e){
            throw new PaymentFailedException("Failed to contact payment provider", e);
        }

        String responseBody = response.getBody();
        if(responseBody == null || responseBody.isBlank()) {
            throw new PaymentFailedException("Payment provider returned empty response");
        }

        final int transactionId;
        try
        {
            transactionId = Integer.parseInt(responseBody.trim());
        }
        catch(NumberFormatException e)
        {
            throw new PaymentFailedException("Invalid response from payment provider: " + responseBody,e);
        }

        if(transactionId ==-1)
        {
            throw new PaymentFailedException("Payment failed for card with 4 last digits: " + paymentInfo.cardNumber().substring(paymentInfo.cardNumber().length() - 4));
        }
        return transactionId;
    }

    // refunds a payment, throws on failure
    public void cancelPayment(int transactionId)
    {
        //prepare the http request
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        requestBody.add("action_type", "refund");
        requestBody.add("transaction_id", String.valueOf(transactionId));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        //send and get the response
        final ResponseEntity<String> response;

        try
        {
            response = restTemplate.postForEntity(BASE_URL, requestEntity, String.class);
        }
        catch (RestClientException e)
        {
            throw new RefundStatusUnknownException("Failed to contact payment provider during refund",e);
        }

        String responseBody = response.getBody();

        if(responseBody == null || responseBody.isBlank())
        {
            throw new RefundStatusUnknownException("Payment provider returned empty refund response");
        }

        final int refundResult;

        try
        {
            refundResult = Integer.parseInt(responseBody.trim());
        }
        catch(NumberFormatException e)
        {
            throw new RefundStatusUnknownException("Invalid refund response from payment provider: " + responseBody,e);
        }

        if(refundResult != 1)
        {
            throw new RefundFailedException("Refund failed for transaction " + transactionId);
        }
    }

}
