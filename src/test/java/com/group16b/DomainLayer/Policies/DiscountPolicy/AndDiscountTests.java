package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AndDiscountTests {

    private final DiscountContext matchingContext =
            new DiscountContext(
                    20,
                    3,
                    LocalDateTime.now(),
                    null);

    @Test
    public void constructor_nullLeft_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AndDiscount(
                        null,
                        new SimpleDiscount(10),
                        20));
    }

    @Test
    public void constructor_nullRight_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AndDiscount(
                        new SimpleDiscount(10),
                        null,
                        20));
    }

    @Test
    public void constructor_invalidPercentage_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AndDiscount(
                        new SimpleDiscount(10),
                        new SimpleDiscount(20),
                        101));
    }

    @Test
    public void calculateDiscount_bothConditionsMet_appliesDiscount() {
        AndDiscount discount = new AndDiscount(
                new AmountRangeDiscount(2, null, 10),
                new AmountRangeDiscount(null, 5, 10),
                30);

        assertTrue(discount.isMet(matchingContext));

        assertEquals(
                70.0,
                discount.calculateDiscount(
                        100.0,
                        matchingContext),
                0.001);
    }

    @Test
    public void calculateDiscount_leftConditionFails_returnsOriginal() {
        AndDiscount discount = new AndDiscount(
                new AmountRangeDiscount(4, null, 10),
                new AmountRangeDiscount(null, 5, 10),
                30);

        assertFalse(discount.isMet(matchingContext));

        assertEquals(
                100.0,
                discount.calculateDiscount(
                        100.0,
                        matchingContext),
                0.001);
    }

    @Test
    public void calculateDiscount_rightConditionFails_returnsOriginal() {
        AndDiscount discount = new AndDiscount(
                new AmountRangeDiscount(2, null, 10),
                new AmountRangeDiscount(null, 2, 10),
                30);

        assertFalse(discount.isMet(matchingContext));

        assertEquals(
                100.0,
                discount.calculateDiscount(
                        100.0,
                        matchingContext),
                0.001);
    }
}