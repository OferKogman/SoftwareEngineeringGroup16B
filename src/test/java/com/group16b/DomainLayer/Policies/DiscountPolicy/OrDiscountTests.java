package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import java.util.List;
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
        OrDiscount or = new OrDiscount(List.of(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0)
        ), 20);
        assertEquals(80.0, or.calculateDiscount(100.0, ctx(0, 1)), 0.001);
    }

    @Test
    public void testIsMetTrueWhenOneChildMet() {
        OrDiscount or = new OrDiscount(List.of(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0)
        ), 20);
        assertTrue(or.isMet(ctx(0, 1)));
    }

    @Test
    public void testAllConditionsMetAppliesDiscount() {
        // Both met → Or still applies (any is enough)
        OrDiscount or = new OrDiscount(List.of(
                new AmountRangeDiscount(1, 10, 0),
                new SimpleDiscount(0)
        ), 15);
        assertEquals(85.0, or.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    @Test
    public void testSingleChildMet() {
        OrDiscount or = new OrDiscount(List.of(new SimpleDiscount(0)), 10);
        assertEquals(90.0, or.calculateDiscount(100.0, ctx(0, 0)), 0.001);
    }

    // ---- Negative: no condition met → no discount ----

    @Test
    public void testNoConditionMetNoDiscount() {
        // tickets=10 > max=5 → AmountRange not met
        // tickets=10 > max=3 → second AmountRange not met
        // OR: none met → no discount
        OrDiscount or = new OrDiscount(List.of(
                new AmountRangeDiscount(null, 5, 0),
                new AmountRangeDiscount(null, 3, 0)
        ), 20);
        assertEquals(100.0, or.calculateDiscount(100.0, ctx(0, 10)), 0.001);
    }

    @Test
    public void testIsMetFalseWhenNoChildMet() {
        OrDiscount or = new OrDiscount(List.of(
                new AmountRangeDiscount(null, 5, 0),
                new AmountRangeDiscount(null, 3, 0)
        ), 20);
        assertFalse(or.isMet(ctx(0, 10)));
    }

    // ---- Nested: Or inside And (the appendix's example) ----

    @Test
    public void testNestedOrInsideAnd() {
        // "18+ AND (≤2 tickets OR ≥100 tickets)"
        // age=20, tickets=1 → 18+ met, ≤2 met → whole thing met
        OrDiscount innerOr = new OrDiscount(List.of(
                new AmountRangeDiscount(null, 2, 0),   // max 2
                new AmountRangeDiscount(100, null, 0)  // min 100
        ), 0);
        AndDiscount outer = new AndDiscount(List.of(innerOr, new SimpleDiscount(0)), 30);
        // SimpleDiscount(0) stands in for an age check (always true here)
        assertEquals(70.0, outer.calculateDiscount(100.0, ctx(20, 1)), 0.001);
    }

    @Test
    public void testNestedOrInsideAndNotMet() {
        // age=20, tickets=5 → 18+ met, but 5 is NOT ≤2 and NOT ≥100 → Or not met → And not met
        OrDiscount innerOr = new OrDiscount(List.of(
                new AmountRangeDiscount(null, 2, 0),
                new AmountRangeDiscount(100, null, 0)
        ), 0);
        AndDiscount outer = new AndDiscount(List.of(innerOr, new SimpleDiscount(0)), 30);
        assertEquals(100.0, outer.calculateDiscount(100.0, ctx(20, 5)), 0.001);
    }

    // ---- Constructor validation ----

    @Test
    public void testNullChildrenThrows() {
        assertThrows(IllegalArgumentException.class, () -> new OrDiscount(null, 10));
    }

    @Test
    public void testEmptyChildrenThrows() {
        assertThrows(IllegalArgumentException.class, () -> new OrDiscount(List.of(), 10));
    }

    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrDiscount(List.of(new SimpleDiscount(0)), -1));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrDiscount(List.of(new SimpleDiscount(0)), 101));
    }
}