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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.Exceptions.IllegalPaymentInfoException;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.InfrastructureLayer.PaymentService;


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


    

}
