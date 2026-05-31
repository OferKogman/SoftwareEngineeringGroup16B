package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AmountRangeDiscountTests {

    // Positive tests
    @Test
    public void testMinAndMaxRange() {
        AmountRangeDiscount discount = new AmountRangeDiscount(2, 5, 10);
        assertEquals(90.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testMinOnlyNoMax() {
        AmountRangeDiscount discount = new AmountRangeDiscount(2, null, 20);
        assertEquals(80.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testMaxOnlyNoMin() {
        AmountRangeDiscount discount = new AmountRangeDiscount(null, 5, 15);
        assertEquals(85.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testNoBoundsUnlimited() {
        AmountRangeDiscount discount = new AmountRangeDiscount(null, null, 50);
        assertEquals(50.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testZeroPercentNoDiscount() {
        AmountRangeDiscount discount = new AmountRangeDiscount(1, 10, 0);
        assertEquals(100.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testHundredPercentFree() {
        AmountRangeDiscount discount = new AmountRangeDiscount(1, 10, 100);
        assertEquals(0.0, discount.calculateDiscount(100.0), 0.001);
    }

    // Negative tests
    @Test
    public void testMinTicketsBelowOneThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AmountRangeDiscount(0, 5, 10));
    }

    @Test
    public void testMaxTicketsBelowOneThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AmountRangeDiscount(1, 0, 10));
    }

    @Test
    public void testMinExceedsMaxThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AmountRangeDiscount(10, 5, 10));
    }

    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AmountRangeDiscount(1, 5, -1));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AmountRangeDiscount(1, 5, 101));
    }
}