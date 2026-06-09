package com.group16b.InfrastructureLayer;

import com.group16b.ApplicationLayer.Exceptions.IllegalPaymentInfoException;
import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Exceptions.PaymentStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.RefundFailedException;
import com.group16b.ApplicationLayer.Exceptions.RefundStatusUnknownException;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Records.PaymentInfo;

import java.math.BigDecimal;
import java.time.YearMonth;

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
    private static final int MIN_TRANSACTION_ID=10000;
    private static final int MAX_TRANSACTION_ID=100000;


    private static final String BASE_URL="https://damp-lynna-wsep-1984852e.koyeb.app/";

    private final RestTemplate restTemplate;

    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //pay, returns the transaction id if successful, or throw on failure
    public int processPayment(PaymentInfo paymentInfo, double amount)
    {   
        validatePaymentInfo(paymentInfo,amount);
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
            throw new PaymentStatusUnknownException("Failed to contact payment provider", e);
        }

        String responseBody = response.getBody();
        if(responseBody == null || responseBody.isBlank()) {
            throw new PaymentStatusUnknownException("Payment provider returned empty response");
        }

        final int transactionId;
        try
        {
            transactionId = Integer.parseInt(responseBody.trim());
        }
        catch(NumberFormatException e)
        {
            throw new PaymentStatusUnknownException("Invalid response from payment provider: " + responseBody,e);
        }

        if(transactionId ==-1)
        {
            throw new PaymentFailedException("Payment was rejected by payment provider");
        }
        if(!isValidTransactionId(transactionId))
            throw new PaymentStatusUnknownException("Provider returned invalid transaction id: "+transactionId);
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
            throw new RefundStatusUnknownException("Failed to contact payment provider during refund: " + transactionId +".", e);
        }

        String responseBody = response.getBody();

        if(responseBody == null || responseBody.isBlank())
        {
            throw new RefundStatusUnknownException("Payment provider returned empty refund response for: " + transactionId);
        }

        final int refundResult;

        try
        {
            refundResult = Integer.parseInt(responseBody.trim());
        }
        catch(NumberFormatException e)
        {
            throw new RefundStatusUnknownException("Invalid refund response from payment provider during refund for: " + transactionId + ", response: " + responseBody, e);
        }

        if(refundResult == -1)
        {
            throw new RefundFailedException("Refund failed for transaction " + transactionId);
        }

        if(refundResult != 1)
        {
            throw new RefundStatusUnknownException("Provider returned invalid refund result: " + refundResult + ", for transaction " + transactionId);
        }
    }

    //can be expanded as needed, like verify number is legit
    //validate id is legit, etc.
    //ill let someone else do it if they want
    private static void validatePaymentInfo(PaymentInfo paymentInfo, double amount)
    {
        if(paymentInfo == null)
            throw new IllegalPaymentInfoException("Payment information is required");
        if(amount <= 0)
            throw new IllegalPaymentInfoException("Amount must be positive");

        if(paymentInfo.cardNumber() == null || paymentInfo.cardNumber().isBlank())
            throw new IllegalPaymentInfoException("Card number is required");

        if(paymentInfo.cvv() == null || paymentInfo.cvv().isBlank())
            throw new IllegalPaymentInfoException("CVV is required");

        if(paymentInfo.holder()==null || paymentInfo.holder().isBlank())
            throw new IllegalPaymentInfoException("card holder is blank");

        if(paymentInfo.id() == null || paymentInfo.id().isBlank())
            throw new IllegalPaymentInfoException("card holder id is blank");
        validateExpirationDate(paymentInfo.year(), paymentInfo.month());
    }

    
    private static void validateExpirationDate(int year, int month)
    {
        if(month < 1 || month > 12)
            throw new IllegalPaymentInfoException(
                "Invalid expiration month: " + month);

        YearMonth expiration = YearMonth.of(year, month);

        if(expiration.isBefore(YearMonth.now()))
            throw new IllegalPaymentInfoException(
                "Card is expired");
    }

    private static boolean isValidTransactionId(int id)
    {
        return id >= MIN_TRANSACTION_ID && id <= MAX_TRANSACTION_ID;
    }

    
}
