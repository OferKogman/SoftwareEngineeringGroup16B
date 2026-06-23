package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SumDiscountTests {

    private DiscountContext dc1 = new DiscountContext(10, 3, null, "SAVE10");

    // --- Constructor ---
    @Test
    public void testNullLeftThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new SumDiscount(null, new SimpleDiscount(0)));
    }

    @Test
    public void testNullRightThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new SumDiscount(new SimpleDiscount(0), null));
    }

    // --- Single policy (baseline) ---

    @Test
    public void testSinglePolicyApplied() {
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(15),
                new SimpleDiscount(0));  // right child does nothing
        assertEquals(85.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testBothZeroPercentNoChange() {
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(0),
                new SimpleDiscount(0));
        assertEquals(100.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testBothHundredPercentReturnsZero() {
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(100),
                new SimpleDiscount(0));  // left already zero'd it
        assertEquals(0.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    // --- Stacked discounts (chained, each on remaining price) ---

    @Test
    public void testTwoPoliciesChained() {
        // 10% on 100 = 90, then 20% on 90 = 72
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(20));
        assertEquals(72.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testNestedSumThreeChained() {
        // 10% on 100 = 90, then (10% on 90 = 81, then 10% on 81 = 72.9)
        SumDiscount innerSum = new SumDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(10));
        SumDiscount outerSum = new SumDiscount(
                new SimpleDiscount(10),
                innerSum);
        assertEquals(72.9, outerSum.calculateDiscount(100.0, dc1), 0.001);
    }

    // --- Does not go below zero ---

    @Test
    public void testDoesNotGoBelowZero() {
        // 100% wipes to 0, second policy has nothing left to discount
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(100),
                new SimpleDiscount(50));
        assertEquals(0.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    // --- Mixed conditions ---

    @Test
    public void testSumWithConditionalDiscount() {
        // First: 20% simple always applies = 80
        // Second: 10% on amount range, tickets=3 (within 2-5) applies = 72
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(20),
                new AmountRangeDiscount(2, 5, 10));
        assertEquals(72.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testSumWithFailingConditional() {
        // First: 20% simple = 80
        // Second: 10% on amount range, but tickets=1 (outside 2-5) → doesn't apply = 80
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(20),
                new AmountRangeDiscount(2, 5, 10));
        assertEquals(80.0, sum.calculateDiscount(100.0, new DiscountContext(0, 1, null, null)), 0.001);
    }
}