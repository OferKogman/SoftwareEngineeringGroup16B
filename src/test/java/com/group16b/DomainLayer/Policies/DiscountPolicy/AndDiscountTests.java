package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AndDiscountTests {

    private DiscountContext ctx(int age, int ticketCount) {
        return new DiscountContext(age, ticketCount, java.time.LocalDateTime.now(), null);
    }

    @Test
    public void testAllConditionsMetAppliesDiscount() {
        AndDiscount and = new AndDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20
        );
        assertEquals(80.0, and.calculateDiscount(100.0, ctx(20, 3)), 0.001);
    }

    @Test
    public void testIsMetTrueWhenAllChildrenMet() {
        AndDiscount and = new AndDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20
        );
        assertTrue(and.isMet(ctx(20, 3)));
    }

    @Test
    public void testOneChildNotMetNoDiscount() {
        AndDiscount and = new AndDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20
        );
        assertEquals(100.0, and.calculateDiscount(100.0, ctx(20, 1)), 0.001);
    }

    @Test
    public void testIsMetFalseWhenOneChildNotMet() {
        AndDiscount and = new AndDiscount(
                new AmountRangeDiscount(2, 5, 0),
                new SimpleDiscount(0),
                20
        );
        assertFalse(and.isMet(ctx(20, 1)));
    }

    @Test
    public void testNestedAndAllMet() {
        AndDiscount inner = new AndDiscount(
                new AmountRangeDiscount(1, 10, 0),
                new AmountRangeDiscount(1, 10, 0),
                0
        );
        AndDiscount outer = new AndDiscount(inner, new SimpleDiscount(0), 25);
        assertEquals(75.0, outer.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    @Test
    public void testNestedAndInnerNotMet() {
        AndDiscount inner = new AndDiscount(
                new AmountRangeDiscount(1, 2, 0),
                new SimpleDiscount(0),
                0
        );
        AndDiscount outer = new AndDiscount(inner, new SimpleDiscount(0), 25);
        assertEquals(100.0, outer.calculateDiscount(100.0, ctx(0, 5)), 0.001);
    }

    @Test
    public void testNullChildrenThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AndDiscount(null, new SimpleDiscount(0), 10));
    }

    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new AndDiscount(new SimpleDiscount(0), new SimpleDiscount(0), -1));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new AndDiscount(new SimpleDiscount(0), new SimpleDiscount(0), 101));
    }
}