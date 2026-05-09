package com.group16b.DomainLayer.ProductionCompanyPolicy;

public interface IProductionCompanyPolicyRepository {
    /*
     * Adds a production company policy to the repository.
     * 
     * @param productionCompanyPolicy the production company policy to add
     * 
     * @throws IllegalArgumentException if the production company policy is null or
     * already exists
     */
    public void addProductionCompanyPolicy(ProductionCompanyPolicy productionCompanyPolicy);

    /*
     * Retrieves a production company policy by it's ID.
     * 
     * @param id the ID of the production company policy to retrieve
     * 
     * @return the production company policy with the specified ID, or null if not
     * found
     */
    public ProductionCompanyPolicy getProductionCompanyPolicyById(int id);

    /*
     * Retrieves a production company policy by it's name.
     * 
     * @param name the name of the production company policy to retrieve
     * 
     * @return the production company policy with the specified name, or null if not
     * found
     */
    public ProductionCompanyPolicy getProductionCompanyPolicyByName(String name);
}
