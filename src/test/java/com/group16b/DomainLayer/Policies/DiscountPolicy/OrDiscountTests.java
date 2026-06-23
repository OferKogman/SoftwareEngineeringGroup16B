package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrDiscountTests {

    private DiscountContext ctx(int age, int ticketCount) {
        return new DiscountContext(age, ticketCount, java.time.LocalDateTime.now(), null);
    }

    // ---- Positive: at least one condition met → discount applies ----

    @Test
    public void testOneOfTwoConditionsMetAppliesDiscount() {
        // AmountRange(2-5): tickets=1 → NOT met
        // SimpleDiscount: always met
        // OR: at least one met → applies
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20);
        assertEquals(80.0, or.calculateDiscount(100.0, ctx(0, 1)), 0.001);
    }

    @Test
    public void testIsMetTrueWhenOneChildMet() {
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20);
        assertTrue(or.isMet(ctx(0, 1)));
    }

    @Test
    public void testAllConditionsMetAppliesDiscount() {
        // Both met → Or still applies (any is enough)
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(1, 10, 0),
                new SimpleDiscount(0),
                15);
        assertEquals(85.0, or.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    @Test
    public void testBothChildrenApplied() {
        OrDiscount or = new OrDiscount(
                new SimpleDiscount(10),
                new SimpleDiscount(20),
                5);
        assertEquals(95.0, or.calculateDiscount(100.0, ctx(0, 0)), 0.001);
    }

    // ---- Negative: no condition met → no discount ----

    @Test
    public void testNoConditionMetNoDiscount() {
        // tickets=10 > max=5 → AmountRange not met
        // SimpleDiscount with 0% doesn't help → OR: none met → no discount
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(null, 5, 0),
                new AmountRangeDiscount(null, 3, 0),
                20);
        assertEquals(100.0, or.calculateDiscount(100.0, ctx(0, 10)), 0.001);
    }

    @Test
    public void testIsMetFalseWhenBothChildrenNotMet() {
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(null, 5, 0),
                new AmountRangeDiscount(null, 3, 0),
                20);
        assertFalse(or.isMet(ctx(0, 10)));
    }

    // ---- Nested: Or inside Or ----

    @Test
    public void testNestedOrMultiplePaths() {
        // Inner: (AmountRange(1-3) OR AmountRange(5-10))
        // Outer: Inner OR SimpleDiscount
        OrDiscount inner = new OrDiscount(
                new AmountRangeDiscount(1, 3, 0),
                new AmountRangeDiscount(5, 10, 0),
                0);
        OrDiscount outer = new OrDiscount(inner, new SimpleDiscount(0), 20);
        // tickets=5 → inner is met via right child → outer is met
        assertEquals(80.0, outer.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    @Test
    public void testNestedOrNoPathMet() {
        OrDiscount inner = new OrDiscount(
                new AmountRangeDiscount(1, 3, 0),
                new AmountRangeDiscount(5, 10, 0),
                0);
        OrDiscount outer = new OrDiscount(inner, new AmountRangeDiscount(1, 2, 0), 20);
        // tickets=4 → doesn't match any range → no discount
        assertEquals(100.0, outer.calculateDiscount(100.0, ctx(0, 4)), 0.001);
    }

    // ---- Constructor validation ----

    @Test
    public void testNullLeftThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrDiscount(null, new SimpleDiscount(0), 10));
    }

    @Test
    public void testNullRightThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrDiscount(new SimpleDiscount(0), null, 10));
    }

    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrDiscount(new SimpleDiscount(0), new SimpleDiscount(0), -1));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrDiscount(new SimpleDiscount(0), new SimpleDiscount(0), 101));
    }
}