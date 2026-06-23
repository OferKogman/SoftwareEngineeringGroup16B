package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MaxDiscountTests {

    private DiscountContext dc1 = new DiscountContext(10, 3, null, "SAVE10");

    @Test
    public void testPicksBestDiscount() {
        MaxDiscount inner = new MaxDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(20)
        );
        MaxDiscount max = new MaxDiscount(inner, new SimpleDiscount(30));
        assertEquals(70.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testSinglePolicyApplied() {
        MaxDiscount max = new MaxDiscount(new SimpleDiscount(15), new SimpleDiscount(0));
        assertEquals(85.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testAllSamePercentage() {
        MaxDiscount max = new MaxDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(10)
        );
        assertEquals(90.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testNullPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new MaxDiscount(null, new SimpleDiscount(0)));
    }

    @Test
    public void testNestedMax() {
        MaxDiscount inner = new MaxDiscount(
                new SimpleDiscount(5),
                new SimpleDiscount(15)
        );
        MaxDiscount outer = new MaxDiscount(inner, new SimpleDiscount(25));
        assertEquals(75.0, outer.calculateDiscount(100.0, dc1), 0.001);
    }
}