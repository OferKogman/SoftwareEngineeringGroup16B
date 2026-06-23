package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MaxDiscountTests {
    private DiscountContext dc1 = new DiscountContext(10, 3, null, "SAVE10");

    @Test
    public void testPicksBestDiscount() {
        // Simple 10% = 90
        // Simple 20% = 80
        // Max picks smaller (better) = 80
        MaxDiscount max = new MaxDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(20));
        assertEquals(80.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testReversedPicksBestDiscount() {
        // Simple 30% = 70
        // Simple 10% = 90
        // Max picks smaller (better) = 70
        MaxDiscount max = new MaxDiscount(
                new SimpleDiscount(30),
                new SimpleDiscount(10));
        assertEquals(70.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testAllSamePercentage() {
        MaxDiscount max = new MaxDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(10));
        assertEquals(90.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testNestedMaxThreePolicies() {
        // Inner: max(15%, 25%) = 75
        // Outer: max(75, 5%) = 75
        MaxDiscount inner = new MaxDiscount(
                new SimpleDiscount(15),
                new SimpleDiscount(25));
        MaxDiscount outer = new MaxDiscount(
                inner,
                new SimpleDiscount(5));
        assertEquals(75.0, outer.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testMaxWithConditional() {
        // Left: 15% simple = 85
        // Right: 25% if amount range (2-5, tickets=3) applies = 75
        // Max picks 75
        MaxDiscount max = new MaxDiscount(
                new SimpleDiscount(15),
                new AmountRangeDiscount(2, 5, 25));
        assertEquals(75.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testMaxWithFailingConditional() {
        // Left: 15% simple = 85
        // Right: 25% if amount range, but tickets outside range → doesn't apply = 100
        // Max picks 85
        MaxDiscount max = new MaxDiscount(
                new SimpleDiscount(15),
                new AmountRangeDiscount(5, 10, 25));
        assertEquals(85.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    // ---- Constructor validation ----

    @Test
    public void testNullLeftThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaxDiscount(null, new SimpleDiscount(0)));
    }

    @Test
    public void testNullRightThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaxDiscount(new SimpleDiscount(0), null));
    }
}