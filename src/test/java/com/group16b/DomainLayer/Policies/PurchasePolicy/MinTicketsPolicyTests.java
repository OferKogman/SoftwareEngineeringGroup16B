package com.group16b.DomainLayer.Policies.PurchasePolicy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinTicketsPolicyTests {

    @Test
    public void constructor_validValue_succeeds() {
        MinTicketsPolicy policy = new MinTicketsPolicy(2);

        assertEquals(2, policy.getMinTicketsPerTransaction());
    }

    @Test
    public void constructor_zero_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MinTicketsPolicy(0));
    }

    @Test
    public void validatePurchase_exactMinimum_succeeds() {
        MinTicketsPolicy policy = new MinTicketsPolicy(2);

        assertDoesNotThrow(
                () -> policy.validatePurchase(
                        new PurchaseContext(20, 2)));
    }

    @Test
    public void validatePurchase_aboveMinimum_succeeds() {
        MinTicketsPolicy policy = new MinTicketsPolicy(2);

        assertDoesNotThrow(
                () -> policy.validatePurchase(
                        new PurchaseContext(20, 5)));
    }

    @Test
    public void validatePurchase_belowMinimum_throws() {
        MinTicketsPolicy policy = new MinTicketsPolicy(2);

        PurchasePolicyException exception = assertThrows(
                PurchasePolicyException.class,
                () -> policy.validatePurchase(
                        new PurchaseContext(20, 1)));

        assertEquals(
                "Must purchase at least 2 ticket(s).",
                exception.getMessage());
    }

    @Test
    public void setter_invalidValue_throws() {
        MinTicketsPolicy policy = new MinTicketsPolicy(2);

        assertThrows(
                IllegalArgumentException.class,
                () -> policy.setMinTicketsPerTransaction(0));
    }
}