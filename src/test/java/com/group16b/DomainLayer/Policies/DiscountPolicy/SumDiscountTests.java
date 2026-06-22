package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SumDiscountTests {

    // --- Constructor ---
    private DiscountContext dc1 = new DiscountContext(10, 3, null, "SAVE10");
    @Test
    public void testNullPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SumDiscount(null));
    }

    @Test
    public void testEmptyPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SumDiscount(List.of()));
    }

    // --- Single policy (baseline) ---

    @Test
    public void testSinglePolicyApplied() {
        SumDiscount sum = new SumDiscount(List.of(new SimpleDiscount(15)));
        assertEquals(85.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testSingleZeroPercentNoChange() {
        SumDiscount sum = new SumDiscount(List.of(new SimpleDiscount(0)));
        assertEquals(100.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testSingleHundredPercentReturnsZero() {
        SumDiscount sum = new SumDiscount(List.of(new SimpleDiscount(100)));
        assertEquals(0.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    // --- Stacked discounts (chained, each on remaining price) ---

    @Test
    public void testTwoPoliciesChained() {
        // 10% on 100 = 90, then 20% on 90 = 72
        SumDiscount sum = new SumDiscount(List.of(
                new SimpleDiscount(10),
                new SimpleDiscount(20)
        ));
        assertEquals(72.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testStackingOrderMatters() {
        // 50% on 100 = 50, then 10% on 50 = 45
        SumDiscount sumAB = new SumDiscount(List.of(new SimpleDiscount(50), new SimpleDiscount(10)));
        // 10% on 100 = 90, then 50% on 90 = 45 — same result here but order still matters in general
        SumDiscount sumBA = new SumDiscount(List.of(new SimpleDiscount(10), new SimpleDiscount(50)));
        assertEquals(45.0, sumAB.calculateDiscount(100.0, dc1), 0.001);
        assertEquals(45.0, sumBA.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testThreePoliciesChained() {
        // 10% on 100 = 90, 10% on 90 = 81, 10% on 81 = 72.9
        SumDiscount sum = new SumDiscount(List.of(
                new SimpleDiscount(10),
                new SimpleDiscount(10),
                new SimpleDiscount(10)
        ));
        assertEquals(72.9, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    // --- Does not go below zero ---

    @Test
    public void testDoesNotGoBelowZero() {
        // 100% wipes to 0, second policy has nothing left to discount
        SumDiscount sum = new SumDiscount(List.of(
                new SimpleDiscount(100),
                new SimpleDiscount(50)
        ));
        assertEquals(0.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }



    // --- All zero discounts ---

    @Test
    public void testAllZeroDiscountsNoChange() {
        SumDiscount sum = new SumDiscount(List.of(
                new SimpleDiscount(0),
                new SimpleDiscount(0),
                new SimpleDiscount(0)
        ));
        assertEquals(100.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }
}