package com.group16b.ApplicationLayer.Records;

public record PaymentInfo(String currency, String cardNumber, int month, int year, String holder, String cvv, String id) {
}
