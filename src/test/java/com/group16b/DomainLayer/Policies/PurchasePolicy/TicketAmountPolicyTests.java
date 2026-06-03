package com.group16b.DomainLayer.Policies.PurchasePolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TicketAmountPolicyTests {

    // --- Constructor: valid cases ---

    @Test
    public void testMinOnlyIsValid() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, null);
        assertEquals(2, policy.getMinTickets());
        assertNull(policy.getMaxTickets());
    }

    @Test
    public void testMaxOnlyIsValid() {
        TicketAmountPolicy policy = new TicketAmountPolicy(null, 10);
        assertNull(policy.getMinTickets());
        assertEquals(10, policy.getMaxTickets());
    }

    @Test
    public void testRangeIsValid() {
        assertDoesNotThrow(() -> new TicketAmountPolicy(2, 10));
    }

    @Test
    public void testMinEqualsMaxIsValid() {
        assertDoesNotThrow(() -> new TicketAmountPolicy(5, 5));
    }

    // --- Constructor: invalid cases ---

    @Test
    public void testBothNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TicketAmountPolicy(null, null));
    }

    @Test
    public void testMinZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TicketAmountPolicy(0, null));
    }

    @Test
    public void testMinNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TicketAmountPolicy(-1, null));
    }

    @Test
    public void testMaxZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TicketAmountPolicy(null, 0));
    }

    @Test
    public void testMaxNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TicketAmountPolicy(null, -1));
    }

    @Test
    public void testMinExceedsMaxThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TicketAmountPolicy(10, 5));
    }

    // --- validatePurchase: min-only ---

    @Test
    public void testMinOnlyPassesAtExact() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, null);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 2)));
    }

    @Test
    public void testMinOnlyPassesAboveMin() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, null);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 5)));
    }

    @Test
    public void testMinOnlyFailsBelowMin() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, null);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(20, 1)));
    }

    // --- validatePurchase: max-only ---

    @Test
    public void testMaxOnlyPassesAtExact() {
        TicketAmountPolicy policy = new TicketAmountPolicy(null, 10);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 10)));
    }

    @Test
    public void testMaxOnlyPassesBelowMax() {
        TicketAmountPolicy policy = new TicketAmountPolicy(null, 10);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 5)));
    }

    @Test
    public void testMaxOnlyFailsAboveMax() {
        TicketAmountPolicy policy = new TicketAmountPolicy(null, 10);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(20, 11)));
    }

    // --- validatePurchase: range ---

    @Test
    public void testRangePassesWithinRange() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, 10);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 5)));
    }

    @Test
    public void testRangePassesAtMinBoundary() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, 10);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 2)));
    }

    @Test
    public void testRangePassesAtMaxBoundary() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, 10);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 10)));
    }

    @Test
    public void testRangeFailsBelowMin() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, 10);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(20, 1)));
    }

    @Test
    public void testRangeFailsAboveMax() {
        TicketAmountPolicy policy = new TicketAmountPolicy(2, 10);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(20, 11)));
    }
}