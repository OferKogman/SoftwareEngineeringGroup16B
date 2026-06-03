package com.group16b.DomainLayer.Policies.PurchasePolicy;

public class AgePolicy implements PurchasePolicy {
    private Integer minAge; // null = no minimum
    private Integer maxAge; // null = no maximum

    public AgePolicy(Integer minAge, Integer maxAge) {
        if (minAge == null && maxAge == null)
            throw new IllegalArgumentException("At least one of minAge or maxAge must be defined.");
        if (minAge != null && minAge < 0)
            throw new IllegalArgumentException("Minimum age cannot be negative.");
        if (maxAge != null && maxAge < 0)
            throw new IllegalArgumentException("Maximum age cannot be negative.");
        if (minAge != null && maxAge != null && minAge > maxAge)
            throw new IllegalArgumentException("Minimum age cannot exceed maximum age.");
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public Integer getMinAge() { return minAge; }
    public Integer getMaxAge() { return maxAge; }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException {
        if (minAge != null && context.age() < minAge)
            throw new PurchasePolicyException("Age must be at least " + minAge + ".");
        if (maxAge != null && context.age() > maxAge)
            throw new PurchasePolicyException("Age must be at most " + maxAge + ".");
    }
}