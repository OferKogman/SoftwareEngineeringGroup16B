package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrDiscountTests {

    private DiscountContext ctx(int age, int ticketCount) {
        return new DiscountContext(age, ticketCount, java.time.LocalDateTime.now(), null);
    }

    @Test
    public void testOneOfTwoConditionsMetAppliesDiscount() {
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20
        );
        assertEquals(80.0, or.calculateDiscount(100.0, ctx(0, 1)), 0.001);
    }

    @Test
    public void testIsMetTrueWhenOneChildMet() {
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20
        );
        assertTrue(or.isMet(ctx(0, 1)));
    }

    @Test
    public void testAllConditionsMetAppliesDiscount() {
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(1, 10, 0),
                new SimpleDiscount(0),
                15
        );
        assertEquals(85.0, or.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    @Test
    public void testNoConditionMetNoDiscount() {
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(null, 5, 0),
                new AmountRangeDiscount(null, 3, 0),
                20
        );
        assertEquals(100.0, or.calculateDiscount(100.0, ctx(0, 10)), 0.001);
    }

    @Test
    public void testIsMetFalseWhenNoChildMet() {
        OrDiscount or = new OrDiscount(
                new AmountRangeDiscount(null, 5, 0),
                new AmountRangeDiscount(null, 3, 0),
                20
        );
        assertFalse(or.isMet(ctx(0, 10)));
    }

    @Test
    public void testNestedOrInsideAnd() {
        OrDiscount innerOr = new OrDiscount(
                new AmountRangeDiscount(null, 2, 0),
                new AmountRangeDiscount(100, null, 0),
                0
        );
        AndDiscount outer = new AndDiscount(innerOr, new SimpleDiscount(0), 30);
        assertEquals(70.0, outer.calculateDiscount(100.0, ctx(20, 1)), 0.001);
    }

    @Test
    public void testNestedOrInsideAndNotMet() {
        OrDiscount innerOr = new OrDiscount(
                new AmountRangeDiscount(null, 2, 0),
                new AmountRangeDiscount(100, null, 0),
                0
        );
        AndDiscount outer = new AndDiscount(innerOr, new SimpleDiscount(0), 30);
        assertEquals(100.0, outer.calculateDiscount(100.0, ctx(20, 5)), 0.001);
    }

    @Test
    public void testNullChildrenThrows() {
        assertThrows(IllegalArgumentException.class, () -> new OrDiscount(null, new SimpleDiscount(0), 10));
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