package com.group16b.DomainLayer.ProductionCompanyPolicy;

import java.util.HashMap;
import java.util.Map;

public class ProductionCompanyPolicyRepositoryMapImpl implements IProductionCompanyPolicyRepository {
    private Map<Integer, ProductionCompanyPolicy> productionCompanyPoliciesById;
    private Map<String, ProductionCompanyPolicy> productionCompanyPoliciesByName;

    private static final ProductionCompanyPolicyRepositoryMapImpl instance = new ProductionCompanyPolicyRepositoryMapImpl();

    private ProductionCompanyPolicyRepositoryMapImpl() {
        this.productionCompanyPoliciesById = new HashMap<>();
        this.productionCompanyPoliciesByName = new HashMap<>();
    }

    // singleton pattern to ensure only one instance of the repository exists
    public static synchronized ProductionCompanyPolicyRepositoryMapImpl getInstance() {
        return instance;
    }

    /*
     * Adds a production company policy to the repository.
     * 
     * @param productionCompanyPolicy the production company policy to add
     * 
     * @throws IllegalArgumentException if the prouction company policy is null or
     * if a production company policy with the same ID or name already exists
     */
    @Override
    public void addProductionCompanyPolicy(ProductionCompanyPolicy productionCompanyPolicy) {
        if (productionCompanyPolicy == null) {
            throw new IllegalArgumentException("Production Company Policy cannot be null");
        }
        if (productionCompanyPoliciesById.containsKey(productionCompanyPolicy.getId())) {
            throw new IllegalArgumentException("Production Company Policy with this ID already exists");
        }
        if (productionCompanyPoliciesByName.containsKey(productionCompanyPolicy.getName())) {
            throw new IllegalArgumentException("Production Company Policy with this name already exists");
        }
        productionCompanyPoliciesById.put(productionCompanyPolicy.getId(), productionCompanyPolicy);
        productionCompanyPoliciesByName.put(productionCompanyPolicy.getName(), productionCompanyPolicy);
    }

    // Retrieves a production company policy by it's ID.
    @Override
    public ProductionCompanyPolicy getProductionCompanyPolicyById(int id) {
        return productionCompanyPoliciesById.get(id);
    }

    // Retrieves a production company policy by it's name.
    @Override
    public ProductionCompanyPolicy getProductionCompanyPolicyByName(String name) {
        return productionCompanyPoliciesByName.get(name);
    }
}
