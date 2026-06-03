package com.group16b.DomainLayer.Policies.PurchasePolicy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AgePolicyTests {

    // --- Constructor: valid cases ---

    @Test
    public void testMinAgeOnlyIsValid() {
        AgePolicy policy = new AgePolicy(18, null);
        assertEquals(18, policy.getMinAge());
        assertNull(policy.getMaxAge());
    }

    @Test
    public void testMaxAgeOnlyIsValid() {
        AgePolicy policy = new AgePolicy(null, 65);
        assertNull(policy.getMinAge());
        assertEquals(65, policy.getMaxAge());
    }

    @Test
    public void testRangeIsValid() {
        assertDoesNotThrow(() -> new AgePolicy(18, 65));
    }

    @Test
    public void testMinEqualsMaxIsValid() {
        assertDoesNotThrow(() -> new AgePolicy(18, 18));
    }

    // --- Constructor: invalid cases ---

    @Test
    public void testBothNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AgePolicy(null, null));
    }

    @Test
    public void testNegativeMinAgeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AgePolicy(-1, null));
    }

    @Test
    public void testNegativeMaxAgeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AgePolicy(null, -1));
    }

    @Test
    public void testMinExceedsMaxThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AgePolicy(30, 18));
    }

    // --- validatePurchase: min-only ---

    @Test
    public void testMinOnlyPassesAtExactAge() {
        AgePolicy policy = new AgePolicy(18, null);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(18, 1)));
    }

    @Test
    public void testMinOnlyPassesAboveMin() {
        AgePolicy policy = new AgePolicy(18, null);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(30, 1)));
    }

    @Test
    public void testMinOnlyFailsBelowMin() {
        AgePolicy policy = new AgePolicy(18, null);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(17, 1)));
    }

    // --- validatePurchase: max-only ---

    @Test
    public void testMaxOnlyPassesAtExactAge() {
        AgePolicy policy = new AgePolicy(null, 65);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(65, 1)));
    }

    @Test
    public void testMaxOnlyPassesBelowMax() {
        AgePolicy policy = new AgePolicy(null, 65);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(30, 1)));
    }

    @Test
    public void testMaxOnlyFailsAboveMax() {
        AgePolicy policy = new AgePolicy(null, 65);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(66, 1)));
    }

    // --- validatePurchase: range ---

    @Test
    public void testRangePassesWithinRange() {
        AgePolicy policy = new AgePolicy(18, 65);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(30, 1)));
    }

    @Test
    public void testRangePassesAtMinBoundary() {
        AgePolicy policy = new AgePolicy(18, 65);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(18, 1)));
    }

    @Test
    public void testRangePassesAtMaxBoundary() {
        AgePolicy policy = new AgePolicy(18, 65);
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(65, 1)));
    }

    @Test
    public void testRangeFailsBelowMin() {
        AgePolicy policy = new AgePolicy(18, 65);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(17, 1)));
    }

    @Test
    public void testRangeFailsAboveMax() {
        AgePolicy policy = new AgePolicy(18, 65);
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(66, 1)));
    }
}