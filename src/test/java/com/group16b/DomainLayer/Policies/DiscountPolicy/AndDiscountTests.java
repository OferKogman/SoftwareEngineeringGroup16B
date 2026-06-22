package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AndDiscountTests {

    // Helper — a context with sensible defaults for fields we don't care about
    private DiscountContext ctx(int age, int ticketCount) {
        return new DiscountContext(age, ticketCount, java.time.LocalDateTime.now(), null);
    }

    // ---- Positive: all conditions met → discount applies ----

    @Test
    public void testAllConditionsMetAppliesDiscount() {
        // Both children met: 2–5 tickets AND age 18+
        AndDiscount and = new AndDiscount(List.of(
                new AmountRangeDiscount(2, 5, 0),  // condition only, 0% own discount
                new SimpleDiscount(0)               // always met, 0% own discount
        ), 20);                                     // AndDiscount itself gives 20%
        assertEquals(80.0, and.calculateDiscount(100.0, ctx(20, 3)), 0.001);
    }

    @Test
    public void testIsMetTrueWhenAllChildrenMet() {
        AndDiscount and = new AndDiscount(List.of(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0)
        ), 20);
        assertTrue(and.isMet(ctx(20, 3)));
    }

    @Test
    public void testSingleChildMet() {
        AndDiscount and = new AndDiscount(List.of(new SimpleDiscount(0)), 15);
        assertEquals(85.0, and.calculateDiscount(100.0, ctx(0, 0)), 0.001);
    }

    // ---- Negative: at least one condition NOT met → no discount ----

    @Test
    public void testOneChildNotMetNoDiscount() {
        // tickets = 1, below min of 2 → AmountRange not met → And not met
        AndDiscount and = new AndDiscount(List.of(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0)
        ), 20);
        assertEquals(100.0, and.calculateDiscount(100.0, ctx(20, 1)), 0.001);
    }

    @Test
    public void testIsMetFalseWhenOneChildNotMet() {
        AndDiscount and = new AndDiscount(List.of(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0)
        ), 20);
        assertFalse(and.isMet(ctx(20, 1)));
    }

    @Test
    public void testAllChildrenNotMetNoDiscount() {
        // tickets = 10, above max of 5 → AmountRange not met
        // Coupon code mismatch → CouponCode not met
        AndDiscount and = new AndDiscount(List.of(
                new AmountRangeDiscount(null, 5, 0),
                new SimpleDiscount(0)
        ), 20);
        assertEquals(100.0, and.calculateDiscount(100.0, ctx(20, 10)), 0.001);
    }

    // ---- Nested: And inside And (arbitrary depth) ----

    @Test
    public void testNestedAndAllMet() {
        // Outer AND: inner AND (both amount-range) AND simple
        AndDiscount inner = new AndDiscount(List.of(
                new AmountRangeDiscount(1, 10, 0),
                new AmountRangeDiscount(1, 10, 0)
        ), 0);
        AndDiscount outer = new AndDiscount(List.of(inner, new SimpleDiscount(0)), 25);
        assertEquals(75.0, outer.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    @Test
    public void testNestedAndInnerNotMet() {
        AndDiscount inner = new AndDiscount(List.of(
                new AmountRangeDiscount(1, 2, 0)  // ticketCount=5 > max=2 → not met
        ), 0);
        AndDiscount outer = new AndDiscount(List.of(inner, new SimpleDiscount(0)), 25);
        assertEquals(100.0, outer.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    // ---- Constructor validation ----

    @Test
    public void testNullChildrenThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AndDiscount(null, 10));
    }

    @Test
    public void testEmptyChildrenThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AndDiscount(List.of(), 10));
    }

    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new AndDiscount(List.of(new SimpleDiscount(0)), -1));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new AndDiscount(List.of(new SimpleDiscount(0)), 101));
    }
}