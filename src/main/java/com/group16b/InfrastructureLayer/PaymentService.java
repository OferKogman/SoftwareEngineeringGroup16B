package com.group16b.InfrastructureLayer;

import com.group16b.ApplicationLayer.Exceptions.IllegalPaymentInfoException;
import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Exceptions.PaymentStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.RefundFailedException;
import com.group16b.ApplicationLayer.Exceptions.RefundStatusUnknownException;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class PaymentService implements IPaymentGateway {
    private static final int MIN_TRANSACTION_ID=10000;
    private static final int MAX_TRANSACTION_ID=100000;


    private final WsepClient wsepClient;

    public PaymentService( WsepClient wsepClient) {
        this.wsepClient = wsepClient;
    }

    //pay, returns the transaction id if successful, or throw on failure
    @Override
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

        //send and get the response
        String responseBody =wsepClient.sendRequest(requestBody,
                e -> new PaymentStatusUnknownException("Failed to contact payment provider", e),
                () -> new PaymentStatusUnknownException("Payment provider returned empty response")
            );

        int transactionId =wsepClient.parseIntegerResponse(responseBody,body -> new PaymentStatusUnknownException("Invalid response from payment provider: " + body));

        if(transactionId ==-1)
        {
            throw new PaymentFailedException("Payment was rejected by payment provider");
        }
        if(!isValidTransactionId(transactionId))
            throw new PaymentStatusUnknownException("Provider returned invalid transaction id: "+transactionId);
        return transactionId;
    }

    // refunds a payment, throws on failure
    @Override
    public void cancelPayment(int transactionId)
    {
        if(!isValidTransactionId(transactionId))
            throw new IllegalArgumentException("Illegal transaction id: "+transactionId);
        //prepare the http request
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        requestBody.add("action_type", "refund");
        requestBody.add("transaction_id", String.valueOf(transactionId));

        //send and get the response
        String responseBody = wsepClient.sendRequest(requestBody,  
            e -> new RefundStatusUnknownException("Failed to contact payment provider during refund for transaction id: "+transactionId, e),
            () -> new RefundStatusUnknownException("Payment provider returned empty response during refund for transaction id: "+transactionId));

        int refundResult = wsepClient.parseIntegerResponse(responseBody,body -> new RefundStatusUnknownException("Invalid response from payment provider during refund id: "+transactionId+", response: " + body));
        
        wsepClient.validateSuccessFailureResult(refundResult, 
            ()->new RefundFailedException("Refund failed for transaction " + transactionId), 
            ()->new RefundStatusUnknownException("Provider returned invalid refund result: " + refundResult + ", for transaction " + transactionId));

    }

    //can be expanded as needed, like verify number is legit
    //validate id is legit, etc.
    //ill let someone else do it if they want
    private static void validatePaymentInfo(PaymentInfo paymentInfo, double amount)
    {
        if(paymentInfo == null)
            throw new IllegalPaymentInfoException("Payment information is required");
        if(paymentInfo.currency()==null ||paymentInfo.currency().isBlank())
            throw new IllegalPaymentInfoException("Currency is requiered");
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
