package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AmountRangeDiscountTests {
    private DiscountContext dc1 = new DiscountContext(10, 3, null, null);
    private DiscountContext dc2 = new DiscountContext(10, 10, null, null);
    // Positive tests
    @Test
    public void testMinAndMaxRangeQualifies() {
        AmountRangeDiscount discount = new AmountRangeDiscount(2, 5, 10);
        assertEquals(90.0, discount.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testMinOnlyQualifies() {
        AmountRangeDiscount discount = new AmountRangeDiscount(2, null, 20);
        assertEquals(80.0, discount.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testMaxOnlyQualifies() {
        AmountRangeDiscount discount = new AmountRangeDiscount(null, 5, 15);
        assertEquals(85.0, discount.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testZeroPercentNoDiscount() {
        AmountRangeDiscount discount = new AmountRangeDiscount(1, 10, 0);
        assertEquals(100.0, discount.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testHundredPercentFree() {
        AmountRangeDiscount discount = new AmountRangeDiscount(1, 10, 100);
        assertEquals(0.0, discount.calculateDiscount(100.0, dc1), 0.001);
    }

    // Condition not met — returns original price
    @Test
    public void testBelowMinReturnsOriginal() {
        AmountRangeDiscount discount = new AmountRangeDiscount(5, null, 20);
        assertEquals(100.0, discount.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testAboveMaxReturnsOriginal() {
        AmountRangeDiscount discount = new AmountRangeDiscount(null, 5, 20);
        assertEquals(100.0, discount.calculateDiscount(100.0, dc2), 0.001);
    }

    // Negative tests
    @Test
    public void testBothNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AmountRangeDiscount(null, null, 10));
    }

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