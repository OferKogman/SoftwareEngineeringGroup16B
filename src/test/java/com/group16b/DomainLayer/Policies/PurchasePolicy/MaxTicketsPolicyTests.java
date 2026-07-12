package com.group16b.DomainLayer.Policies.PurchasePolicy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaxTicketsPolicyTests {

    @Test
    public void constructor_validValue_succeeds() {
        MaxTicketsPolicy policy = new MaxTicketsPolicy(4);

        assertEquals(4, policy.getMaxTicketsPerTransaction());
    }

    @Test
    public void constructor_zero_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MaxTicketsPolicy(0));
    }

    @Test
    public void validatePurchase_exactMaximum_succeeds() {
        MaxTicketsPolicy policy = new MaxTicketsPolicy(4);

        assertDoesNotThrow(
                () -> policy.validatePurchase(
                        new PurchaseContext(20, 4)));
    }

    @Test
    public void validatePurchase_belowMaximum_succeeds() {
        MaxTicketsPolicy policy = new MaxTicketsPolicy(4);

        assertDoesNotThrow(
                () -> policy.validatePurchase(
                        new PurchaseContext(20, 2)));
    }

    @Test
    public void validatePurchase_aboveMaximum_throws() {
        MaxTicketsPolicy policy = new MaxTicketsPolicy(4);

        PurchasePolicyException exception = assertThrows(
                PurchasePolicyException.class,
                () -> policy.validatePurchase(
                        new PurchaseContext(20, 5)));

        assertEquals(
                "Cannot purchase more than 4 ticket(s).",
                exception.getMessage());
    }

    @Test
    public void setter_invalidValue_throws() {
        MaxTicketsPolicy policy = new MaxTicketsPolicy(4);

        assertThrows(
                IllegalArgumentException.class,
                () -> policy.setMaxTicketsPerTransaction(0));
    }
}