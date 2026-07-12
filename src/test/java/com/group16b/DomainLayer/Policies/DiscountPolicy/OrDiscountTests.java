package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrDiscountTests {

    private final DiscountContext context =
            new DiscountContext(
                    20,
                    3,
                    LocalDateTime.now(),
                    null);

    @Test
    public void constructor_nullChild_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrDiscount(
                        null,
                        new SimpleDiscount(10),
                        25));
    }

    @Test
    public void constructor_invalidPercentage_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrDiscount(
                        new SimpleDiscount(10),
                        new SimpleDiscount(20),
                        -1));
    }

    @Test
    public void calculateDiscount_leftConditionMet_appliesDiscount() {
        OrDiscount discount = new OrDiscount(
                new AmountRangeDiscount(2, null, 10),
                new AmountRangeDiscount(null, 1, 10),
                25);

        assertTrue(discount.isMet(context));

        assertEquals(
                75.0,
                discount.calculateDiscount(100.0, context),
                0.001);
    }

    @Test
    public void calculateDiscount_rightConditionMet_appliesDiscount() {
        OrDiscount discount = new OrDiscount(
                new AmountRangeDiscount(5, null, 10),
                new AmountRangeDiscount(null, 4, 10),
                25);

        assertTrue(discount.isMet(context));

        assertEquals(
                75.0,
                discount.calculateDiscount(100.0, context),
                0.001);
    }

    @Test
    public void calculateDiscount_neitherConditionMet_returnsOriginal() {
        OrDiscount discount = new OrDiscount(
                new AmountRangeDiscount(5, null, 10),
                new AmountRangeDiscount(null, 2, 10),
                25);

        assertFalse(discount.isMet(context));

        assertEquals(
                100.0,
                discount.calculateDiscount(100.0, context),
                0.001);
    }
}