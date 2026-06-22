package com.group16b.DomainLayer.Policies.DiscountPolicy;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class CouponCodeDiscountTests {
    private DiscountContext dc1 = new DiscountContext(10, 3, null, "SAVE10");
    private DiscountContext dc2 = new DiscountContext(10, 10, null, "SAVE20");
    private DiscountContext dc3 = new DiscountContext(10, 3, null, "SAVE10");
    private DiscountContext dc4 = new DiscountContext(10, 3, null, "SAVE10");
    private DiscountContext dc5 = new DiscountContext(10, 3, LocalDateTime.now().plusSeconds(1), "SAVE10");
    private DiscountContext dc6 = new DiscountContext(10, 3, null, "SAVE10");

    // Positive tests
    @Test
    public void testValidCouponAppliesDiscount() {
        CouponCodeDiscount discount = new CouponCodeDiscount(10, "SAVE10", LocalDateTime.now().plusDays(1), null);
        assertEquals(90.0, discount.calculateDiscount(100, dc1), 0.001);
    }

    @Test
    public void testNoExpiryAppliesDiscount() {
        CouponCodeDiscount discount = new CouponCodeDiscount(20, "SAVE20", null, null);
        assertEquals(80.0, discount.calculateDiscount(100.0, dc2), 0.001);
    }

    @Test
    public void testMaxUsagesNotReachedAppliesDiscount() {
        CouponCodeDiscount discount = new CouponCodeDiscount(10, "SAVE10", null, 3);
        discount.calculateDiscount(100.0, dc3);
        discount.calculateDiscount(100.0, dc3);
        assertEquals(90.0, discount.calculateDiscount(100.0, dc3), 0.001);
    }

    @Test
    public void testMaxUsagesReachedReturnsOriginal() {
        CouponCodeDiscount discount = new CouponCodeDiscount(10, "SAVE10", null, 2);
        discount.calculateDiscount(100.0, dc4);
        discount.calculateDiscount(100.0, dc4);
        assertEquals(100.0, discount.calculateDiscount(100.0, dc4), 0.001);
    }

    @Test
    public void testExpiredCouponReturnsOriginal() {
        CouponCodeDiscount discount = new CouponCodeDiscount(10, "SAVE10", LocalDateTime.now().plusSeconds(1), null);
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        assertEquals(100.0, discount.calculateDiscount(100.0, dc5), 0.001);
    }

    @Test
    public void testUsageCounterIncrements() {
        CouponCodeDiscount discount = new CouponCodeDiscount(10, "SAVE10", null, 5);
        discount.calculateDiscount(100.0, dc6);
        discount.calculateDiscount(100.0, dc6);
        assertEquals(2, discount.getCurrentUsages());
    }

    // Negative tests
    @Test
    public void testNullCodeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CouponCodeDiscount(10, null, null, null));
    }

    @Test
    public void testEmptyCodeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CouponCodeDiscount(10, "", null, null));
    }

    @Test
    public void testExpiredExpiryDateThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CouponCodeDiscount(10, "SAVE10", LocalDateTime.now().minusDays(1), null));
    }

    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CouponCodeDiscount(-1, "SAVE10", null, null));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CouponCodeDiscount(101, "SAVE10", null, null));
    }

    @Test
    public void testMaxUsagesBelowOneThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new CouponCodeDiscount(10, "SAVE10", null, 0));
    }
}