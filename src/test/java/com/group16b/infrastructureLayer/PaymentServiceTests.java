package com.group16b.infrastructureLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.Exceptions.IllegalPaymentInfoException;
import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Exceptions.PaymentStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.RefundFailedException;
import com.group16b.ApplicationLayer.Exceptions.RefundStatusUnknownException;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.InfrastructureLayer.PaymentService;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;


class PaymentServiceTests{
    private RestTemplate restTemplate;
    private PaymentService paymentService;

    private final String TRANSACTION_ID_STRING="12345";
    private final int TRANSACTION_ID= Integer.valueOf(TRANSACTION_ID_STRING);

    private final String LEGAL_CURRENCY="UDS";
    private final String LEGAL_CARD="2222333344445555";
    private final int LEGAL_MONTH=12;
    private final int LEGAL_YEAR=2027;
    private final String LEGAL_HOLDER="Bird Denier";
    private final String LEGAL_CVV="123";
    private final String LEGAL_ID="20444444";

    private final double AMOUNT=100.0;

    private final int MIN_T_ID=10000;
    private final String BELOW_T_MIN_ID=String.valueOf(MIN_T_ID-1);
    private final int MAX_T_ID=100000;
    private final String ABOVE_T_MAX_ID=String.valueOf(MAX_T_ID+1);

    private final int REJECTED_OPERATION=-1;
    private final int ACCEPTED_REFUND_RES=1;

    
    @BeforeEach
    void setup()
    {
        restTemplate=mock(RestTemplate.class);
        paymentService=new PaymentService(new WsepClient(restTemplate));
        ResponseEntity<String> response =new ResponseEntity<>(TRANSACTION_ID_STRING, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(response);
    }

    private PaymentInfo validPayment() {
        return new PaymentInfo(
                LEGAL_CURRENCY,
                LEGAL_CARD,
                LEGAL_MONTH,
                LEGAL_YEAR,
                LEGAL_HOLDER,
                LEGAL_CVV,
                LEGAL_ID
        );
    }

    private PaymentInfo tweakCurrency(String currency)
    {
        return new PaymentInfo(
                currency,
                LEGAL_CARD,
                LEGAL_MONTH,
                LEGAL_YEAR,
                LEGAL_HOLDER,
                LEGAL_CVV,
                LEGAL_ID
        );
    }

    private PaymentInfo tweakCardNumber(String cardNumber) {
        return new PaymentInfo(
                LEGAL_CURRENCY,
                cardNumber,
                LEGAL_MONTH,
                LEGAL_YEAR,
                LEGAL_HOLDER,
                LEGAL_CVV,
                LEGAL_ID
        );
    }

    private PaymentInfo tweakCVV(String cvv) {
        return new PaymentInfo(
                LEGAL_CURRENCY,
                LEGAL_CARD,
                LEGAL_MONTH,
                LEGAL_YEAR,
                LEGAL_HOLDER,
                cvv,
                LEGAL_ID
        );
    }

    private PaymentInfo tweakHolder(String holder) {
        return new PaymentInfo(
                LEGAL_CURRENCY,
                LEGAL_CARD,
                LEGAL_MONTH,
                LEGAL_YEAR,
                holder,
                LEGAL_CVV,
                LEGAL_ID
        );
    }

    private PaymentInfo tweakId(String id) {
        return new PaymentInfo(
                LEGAL_CURRENCY,
                LEGAL_CARD,
                LEGAL_MONTH,
                LEGAL_YEAR,
                LEGAL_HOLDER,
                LEGAL_CVV,
                id
        );
    }

    private PaymentInfo tweakMonth(int month) {
        return new PaymentInfo(
                LEGAL_CURRENCY,
                LEGAL_CARD,
                month,
                LEGAL_YEAR,
                LEGAL_HOLDER,
                LEGAL_CVV,
                LEGAL_ID
        );
    }

    private PaymentInfo tweakDate(int year,int month) {
        return new PaymentInfo(
                LEGAL_CURRENCY,
                LEGAL_CARD,
                month,
                year,
                LEGAL_HOLDER,
                LEGAL_CVV,
                LEGAL_ID
        );
    }


    @Test
    void givenValidPayment_whenProcessPayment_succes()
    {
        int id=paymentService.processPayment(validPayment(), AMOUNT);
        assertEquals(TRANSACTION_ID, id);
    }

    @Test
    void givenEmptyPaymentInfo_whenProcessPayment_IlegalPayInfoException()
    {
        IllegalPaymentInfoException exception=assertThrows(IllegalPaymentInfoException.class,()->paymentService.processPayment(null, AMOUNT));
        assertEquals("Payment information is required", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenInvalidCurrency_whenProcessPayment_IlegalPayInfoException(String currency)
    {
        var exception =assertThrows(IllegalPaymentInfoException.class, ()->paymentService.processPayment(tweakCurrency(currency), AMOUNT));
        
        assertEquals("Currency is requiered", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenMissingCardNumber_whenProcessPayment_IllegalPayInfoException(String cardNumber)
    {
        var ex = assertThrows(IllegalPaymentInfoException.class,() -> paymentService.processPayment(tweakCardNumber(cardNumber), AMOUNT));

        assertEquals("Card number is required", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenMissingCVV_whenProcessPayment_IllegalPayInfoException(String cvv)
    {
        var ex = assertThrows(IllegalPaymentInfoException.class,() -> paymentService.processPayment(tweakCVV(cvv), AMOUNT));

        assertEquals("CVV is required", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenMissingHolder_whenProcessPayment_IllegalPayInfoException(String holder)
    {
        var ex = assertThrows(IllegalPaymentInfoException.class,() -> paymentService.processPayment(tweakHolder(holder), AMOUNT));

        assertEquals("card holder is blank", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void givenMissingId_whenProcessPayment_IllegalPayInfoException(String id)
    {
        var ex = assertThrows(IllegalPaymentInfoException.class,() -> paymentService.processPayment(tweakId(id), AMOUNT));

        assertEquals("card holder id is blank", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13, -1, 20})
    void givenInvalidMonth_whenProcessPayment_IllegalPayInfoException(int month)
    {
        var ex = assertThrows(IllegalPaymentInfoException.class,() -> paymentService.processPayment(tweakMonth(month), AMOUNT));

        assertEquals("Invalid expiration month: " + month, ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0})
    void givenInvalidAmount_whenProcessPayment_IllegalPayInfoException(double amount)
    {
        var ex = assertThrows(IllegalPaymentInfoException.class,() -> paymentService.processPayment(validPayment(), amount));

        assertEquals("Amount must be positive", ex.getMessage());
    }

    @Test
    void givenExpiredCard_whenProcessPayment_IllegalPayInfoException()
    {
        YearMonth expired = YearMonth.now().minusMonths(1);

        var ex = assertThrows(IllegalPaymentInfoException.class,() -> paymentService.processPayment(tweakDate(expired.getYear(), expired.getMonthValue()),AMOUNT));

        assertEquals("Card is expired", ex.getMessage());
    }

    @Test
    void givenProviderUnavailable_whenProcessPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenThrow(new RestClientException("Connection failed"));

        var ex = assertThrows(PaymentStatusUnknownException.class,() -> paymentService.processPayment(validPayment(), AMOUNT));

        assertEquals("Failed to contact payment provider", ex.getMessage());
    }

    @Test
    void givenNullResponse_whenProcessPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        var ex = assertThrows(PaymentStatusUnknownException.class,() -> paymentService.processPayment(validPayment(), AMOUNT));

        assertEquals("Payment provider returned empty response",ex.getMessage());
    }

    @Test
    void givenBlankResponse_whenProcessPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>("   ", HttpStatus.OK));

        var ex = assertThrows(PaymentStatusUnknownException.class,() -> paymentService.processPayment(validPayment(), AMOUNT));

        assertEquals("Payment provider returned empty response",ex.getMessage());
    }

    @Test
    void givenNonNumericResponse_whenProcessPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>("abc", HttpStatus.OK));

        var ex = assertThrows(PaymentStatusUnknownException.class,() -> paymentService.processPayment(validPayment(), AMOUNT));

        assertEquals("Invalid response from payment provider: abc",ex.getMessage());
    }

    @Test
    void givenRejectedPayment_whenProcessPayment_throwPaymentFailed()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(String.valueOf(REJECTED_OPERATION), HttpStatus.OK));

        var ex = assertThrows(PaymentFailedException.class,() -> paymentService.processPayment(validPayment(), AMOUNT));

        assertEquals("Payment was rejected by payment provider",ex.getMessage());
    }

    @Test
    void givenTooSmallTransactionId_whenProcessPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(BELOW_T_MIN_ID, HttpStatus.OK));

        var ex = assertThrows(PaymentStatusUnknownException.class,() -> paymentService.processPayment(validPayment(), AMOUNT));

        assertEquals("Provider returned invalid transaction id: "+BELOW_T_MIN_ID,ex.getMessage());
    }

    @Test
    void givenTooBigTransactionId_whenProcessPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(ABOVE_T_MAX_ID, HttpStatus.OK));

        var ex = assertThrows(PaymentStatusUnknownException.class,() -> paymentService.processPayment(validPayment(), AMOUNT));

        assertEquals("Provider returned invalid transaction id: "+ABOVE_T_MAX_ID,ex.getMessage());
    }

    @Test
    void givenMinTransactionId_whenProcessPayment_success()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(String.valueOf(MIN_T_ID), HttpStatus.OK));

        int id = paymentService.processPayment(validPayment(), AMOUNT);

        assertEquals(MIN_T_ID, id);
    }

    @Test
    void givenMaxTransactionId_whenProcessPayment_success()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(String.valueOf(MAX_T_ID), HttpStatus.OK));

        int id = paymentService.processPayment(validPayment(), AMOUNT);

        assertEquals(MAX_T_ID, id);
    }

    @Test
    void givenValidTransactionId_whenCancelPayment_success()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(String.valueOf(ACCEPTED_REFUND_RES), HttpStatus.OK));

        assertDoesNotThrow(() -> paymentService.cancelPayment(TRANSACTION_ID));
    }

    //T_ID boundary tests
    @Test
    void givenMinTransactionId_whenCancelPayment_success()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(String.valueOf(ACCEPTED_REFUND_RES), HttpStatus.OK));

        assertDoesNotThrow(() -> paymentService.cancelPayment(MIN_T_ID));
    }

    @Test
    void givenMaxTransactionId_whenCancelPayment_success()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(String.valueOf(ACCEPTED_REFUND_RES), HttpStatus.OK));

        assertDoesNotThrow(() -> paymentService.cancelPayment(MAX_T_ID));
    }

    @Test
    void givenTooSmallTransactionId_whenCancelPayment_throwIllegalArgument()
    {
        var ex = assertThrows(IllegalArgumentException.class,() -> paymentService.cancelPayment(MIN_T_ID - 1));

        assertEquals("Illegal transaction id: " + (MIN_T_ID - 1),ex.getMessage());
    }

    @Test
    void givenTooBigTransactionId_whenCancelPayment_throwIllegalArgument()
    {
        var ex = assertThrows(IllegalArgumentException.class,() -> paymentService.cancelPayment(MAX_T_ID + 1));

        assertEquals("Illegal transaction id: " + (MAX_T_ID + 1),ex.getMessage());
    }

    //sad tests
    @Test
    void givenProviderUnavailable_whenCancelPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenThrow(new RestClientException("Connection failed"));

        var ex = assertThrows(RefundStatusUnknownException.class,() -> paymentService.cancelPayment(TRANSACTION_ID));

        assertEquals("Failed to contact payment provider during refund for transaction id: "+ TRANSACTION_ID,ex.getMessage());
    }

    @Test
    void givenNullResponse_whenCancelPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        var ex = assertThrows(RefundStatusUnknownException.class,() -> paymentService.cancelPayment(TRANSACTION_ID));

        assertEquals("Payment provider returned empty response during refund for transaction id: "+ TRANSACTION_ID,ex.getMessage());
    }

    @Test
    void givenBlankResponse_whenCancelPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>("   ", HttpStatus.OK));

        var ex = assertThrows(RefundStatusUnknownException.class,() -> paymentService.cancelPayment(TRANSACTION_ID));

        assertEquals("Payment provider returned empty response during refund for transaction id: " + TRANSACTION_ID,ex.getMessage());
    }

    @Test
    void givenNonNumericResponse_whenCancelPayment_throwStatusUnknown()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>("abc", HttpStatus.OK));

        var ex = assertThrows(RefundStatusUnknownException.class,() -> paymentService.cancelPayment(TRANSACTION_ID));

        assertEquals("Invalid response from payment provider during refund id: " + TRANSACTION_ID + ", response: abc",ex.getMessage());
    }

    @Test
    void givenRefundRejected_whenCancelPayment_throwRefundFailed()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(String.valueOf(REJECTED_OPERATION), HttpStatus.OK));

        var ex = assertThrows(RefundFailedException.class,() -> paymentService.cancelPayment(TRANSACTION_ID));

        assertEquals("Refund failed for transaction " + TRANSACTION_ID,ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "2", "-2", "100", "-100"})
    void givenInvalidRefundResult_whenCancelPayment_throwStatusUnknown(String result)
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(result, HttpStatus.OK));

        var ex = assertThrows(RefundStatusUnknownException.class,() -> paymentService.cancelPayment(TRANSACTION_ID));

        assertEquals("Provider returned invalid refund result: " + result + ", for transaction " + TRANSACTION_ID,ex.getMessage());
    }

}
