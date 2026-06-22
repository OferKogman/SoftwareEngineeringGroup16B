package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MaxDiscountTests {
    private DiscountContext dc1 = new DiscountContext(10, 3, null, "SAVE10");
    @Test
    public void testPicksBestDiscount() {
        MaxDiscount max = new MaxDiscount(List.of(
                new SimpleDiscount(10),
                new SimpleDiscount(20),
                new SimpleDiscount(30)
        ));
        assertEquals(70.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testSinglePolicyApplied() {
        MaxDiscount max = new MaxDiscount(List.of(new SimpleDiscount(15)));
        assertEquals(85.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testAllSamePercentage() {
        MaxDiscount max = new MaxDiscount(List.of(
                new SimpleDiscount(10),
                new SimpleDiscount(10)
        ));
        assertEquals(90.0, max.calculateDiscount(100.0, dc1), 0.001);
    }

    @Test
    public void testNullPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new MaxDiscount(null));
    }

    @Test
    public void testEmptyPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new MaxDiscount(List.of()));
    }
}