package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SumDiscountTests {

    @Test
    public void testStacksDiscounts() {
        SumDiscount sum = new SumDiscount(List.of(
                new SimpleDiscount(10),
                new SimpleDiscount(20)
        ));
        // 10 saved + 20 saved = 30 total saved from 100
        assertEquals(70.0, sum.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testSinglePolicyApplied() {
        SumDiscount sum = new SumDiscount(List.of(new SimpleDiscount(15)));
        assertEquals(85.0, sum.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testDoesNotGoBelowZero() {
        SumDiscount sum = new SumDiscount(List.of(
                new SimpleDiscount(60),
                new SimpleDiscount(60)
        ));
        assertEquals(0.0, sum.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testNullPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SumDiscount(null));
    }

    @Test
    public void testEmptyPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SumDiscount(List.of()));
    }
}