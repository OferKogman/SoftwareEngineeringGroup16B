package com.group16b.DomainLayer.Policies.PurchasePolicy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PurchasePolicySetConverterTests {

    @Test
    public void nestedPolicySurvivesDatabaseRoundTrip() {
        PurchasePolicy policy = new AndPolicy(
                new ArrayList<>(List.of(
                        new AgePolicy(18, null),
                        new OrPolicy(
                                new ArrayList<>(List.of(
                                        new MinTicketsPolicy(4),
                                        new MaxTicketsPolicy(1)))))));

        Set<PurchasePolicy> policies = new HashSet<>();
        policies.add(policy);

        PurchasePolicySetConverter converter =
                new PurchasePolicySetConverter();

        String databaseValue =
                converter.convertToDatabaseColumn(policies);

        Set<PurchasePolicy> restoredPolicies =
                converter.convertToEntityAttribute(databaseValue);

        assertEquals(1, restoredPolicies.size());

        PurchasePolicy restored =
                restoredPolicies.iterator().next();

        assertDoesNotThrow(
                () -> restored.validatePurchase(
                        new PurchaseContext(20, 4)));

        assertDoesNotThrow(
                () -> restored.validatePurchase(
                        new PurchaseContext(20, 1)));

        assertThrows(
                PurchasePolicyException.class,
                () -> restored.validatePurchase(
                        new PurchaseContext(17, 4)));

        assertThrows(
                PurchasePolicyException.class,
                () -> restored.validatePurchase(
                        new PurchaseContext(20, 2)));
    }
}