package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class DateRangeDiscountTests {

    // Positive tests
    @Test
    public void testStartAndEndRangeQualifies() {
        DateRangeDiscount discount = new DateRangeDiscount(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                10
        );
        assertEquals(90.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testStartOnlyQualifies() {
        DateRangeDiscount discount = new DateRangeDiscount(
                LocalDateTime.now().minusDays(1),
                null,
                20
        );
        assertEquals(80.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testEndOnlyQualifies() {
        DateRangeDiscount discount = new DateRangeDiscount(
                null,
                LocalDateTime.now().plusDays(1),
                15
        );
        assertEquals(85.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testZeroPercentNoDiscount() {
        DateRangeDiscount discount = new DateRangeDiscount(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                0
        );
        assertEquals(100.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testHundredPercentFree() {
        DateRangeDiscount discount = new DateRangeDiscount(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                100
        );
        assertEquals(0.0, discount.calculateDiscount(100.0), 0.001);
    }

    // Condition not met — returns original price
    @Test
    public void testBeforeStartReturnsOriginal() {
        DateRangeDiscount discount = new DateRangeDiscount(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                20
        );
        assertEquals(100.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testAfterEndReturnsOriginal() {
        DateRangeDiscount discount = new DateRangeDiscount(
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1),
                20
        );
        assertEquals(100.0, discount.calculateDiscount(100.0), 0.001);
    }

    // Negative tests
    @Test
    public void testBothNullThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new DateRangeDiscount(null, null, 10));
    }

    @Test
    public void testStartAfterEndThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new DateRangeDiscount(
                        LocalDateTime.now().plusDays(5),
                        LocalDateTime.now().plusDays(1),
                        10
                ));
    }

    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new DateRangeDiscount(null, LocalDateTime.now().plusDays(1), -1));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new DateRangeDiscount(null, LocalDateTime.now().plusDays(1), 101));
    }
}