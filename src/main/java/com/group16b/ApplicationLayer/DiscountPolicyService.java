package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DiscountPolicyService {
    private static final Logger logger = LoggerFactory.getLogger(DiscountPolicyService.class);

    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo;
    private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

    public DiscountPolicyService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepository, IEventRepository eventRepo, IRepository<User> userRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }

    // #368
    public Result<Boolean> createCompanyDiscountPolicy(String sessionToken, int companyID, DiscountPolicy policy) {
        try {
            logger.info("DiscountPolicyService.createCompanyDiscountPolicy: Received request for company ID: {}", companyID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            company.validateUserPermissions(userID, ManagerPermissions.DISCOUNT_POLICY);
            company.addDiscountPolicy(policy);
            productionCompanyRepository.save(company);

            logger.info("DiscountPolicyService.createCompanyDiscountPolicy: Policy added to company {} successfully", companyID);
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("DiscountPolicyService.createCompanyDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("DiscountPolicyService.createCompanyDiscountPolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
}