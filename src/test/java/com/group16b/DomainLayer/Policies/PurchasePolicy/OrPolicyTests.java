package com.group16b.DomainLayer.Policies.PurchasePolicy;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class OrPolicyTests {

    @Test
    public void testNullPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new OrPolicy(null));
    }

    @Test
    public void testEmptyPoliciesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new OrPolicy(List.of()));
    }

    @Test
    public void testFirstPolicyPassSucceeds() {
        OrPolicy policy = new OrPolicy(List.of(
                new AgePolicy(18, null),
                new MinTicketsPolicy(5)
        ));
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 1)));
    }

    @Test
    public void testSecondPolicyPassSucceeds() {
        OrPolicy policy = new OrPolicy(List.of(
                new AgePolicy(18, null),
                new MinTicketsPolicy(5)
        ));
        // age fails (17 < 18) but ticket count passes (5 >= 5)
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(17, 5)));
    }

    @Test
    public void testAllPoliciesPassSucceeds() {
        OrPolicy policy = new OrPolicy(List.of(
                new AgePolicy(18, null),
                new MinTicketsPolicy(2)
        ));
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(20, 3)));
    }

    @Test
    public void testSinglePolicyPassSucceeds() {
        OrPolicy policy = new OrPolicy(List.of(new AgePolicy(18, null)));
        assertDoesNotThrow(() -> policy.validatePurchase(new PurchaseContext(18, 1)));
    }

    @Test
    public void testSinglePolicyFailThrows() {
        OrPolicy policy = new OrPolicy(List.of(new AgePolicy(18, null)));
        assertThrows(PurchasePolicyException.class,
                () -> policy.validatePurchase(new PurchaseContext(17, 1)));
    }
}