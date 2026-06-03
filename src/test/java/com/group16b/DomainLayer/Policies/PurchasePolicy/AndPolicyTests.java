package com.group16b.DomainLayer.Policies.PurchasePolicy;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AndPolicyTests {

    @Test
    public void testNullPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AndPolicy(null));
    }

    @Test
    public void testEmptyPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AndPolicy(List.of()));
    }

    @Test
    public void testAllPoliciesPassSucceeds() {
        AndPolicy policy = new AndPolicy(List.of(
                new AgePolicy(18, null),
                new MinTicketsPolicy(2)
        ));
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 3)));
    }

    @Test
    public void testFirstPolicyFailsThrows() {
        AndPolicy policy = new AndPolicy(List.of(
                new AgePolicy(18, null),
                new MinTicketsPolicy(2)
        ));
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(17, 3)));
    }


    @Test
    public void testAllPoliciesFailThrows() {
        AndPolicy policy = new AndPolicy(List.of(
                new AgePolicy(18, null),
                new MinTicketsPolicy(2)
        ));
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(17, 1)));
    }

    @Test
    public void testSinglePolicyPassSucceeds() {
        AndPolicy policy = new AndPolicy(List.of(new AgePolicy(18, null)));
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(18, 1)));
    }

    @Test
    public void testSinglePolicyFailThrows() {
        AndPolicy policy = new AndPolicy(List.of(new AgePolicy(18, null)));
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(17, 1)));
    }
}