package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SumDiscountTests {

    private DiscountContext dc1 = new DiscountContext(10, 3, null, "SAVE10");

    @Test
    public void testNullPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SumDiscount(null, new SimpleDiscount(0)));
    }

    @Test
    public void testSinglePolicyApplied() {
        SumDiscount sum = new SumDiscount(new SimpleDiscount(15), new SimpleDiscount(0));
        assertEquals(85.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testSingleZeroPercentNoChange() {
        SumDiscount sum = new SumDiscount(new SimpleDiscount(0), new SimpleDiscount(0));
        assertEquals(100.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testTwoPoliciesChained() {
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(20)
        );
        assertEquals(72.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testDoesNotGoBelowZero() {
        SumDiscount sum = new SumDiscount(
                new SimpleDiscount(100),
                new SimpleDiscount(50)
        );
        assertEquals(0.0, sum.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testNestedSum() {
        SumDiscount inner = new SumDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(10)
        );
        SumDiscount outer = new SumDiscount(inner, new SimpleDiscount(10));
        assertEquals(72.9, outer.calculateDiscount(100.0, dc1), 0.001);
    }
}