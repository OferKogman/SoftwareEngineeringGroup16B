package com.group16b.DomainLayer.Policies.DiscountPolicy;

import com.group16b.DomainLayer.Policies.DiscountPolicy.SimpleDiscount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleDiscountTests {

    // Positive tests
    @Test
    public void testTenPercentDiscount() {
        SimpleDiscount discount = new SimpleDiscount(10);
        assertEquals(90.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testZeroPercentReturnsOriginalPrice() {
        SimpleDiscount discount = new SimpleDiscount(0);
        assertEquals(100.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testHundredPercentReturnsZero() {
        SimpleDiscount discount = new SimpleDiscount(100);
        assertEquals(0.0, discount.calculateDiscount(100.0), 0.001);
    }

    @Test
    public void testFiftyPercentDiscount() {
        SimpleDiscount discount = new SimpleDiscount(50);
        assertEquals(50.0, discount.calculateDiscount(100.0), 0.001);
    }

    // Negative tests
    @Test
    public void testNegativePercentageThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleDiscount(-1));
    }

    @Test
    public void testPercentageOverHundredThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleDiscount(101));
    }
}