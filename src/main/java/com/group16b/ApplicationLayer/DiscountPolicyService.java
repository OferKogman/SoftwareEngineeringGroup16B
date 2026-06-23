package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicyDTO;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;

@Service
public class DiscountPolicyService {
    private static final Logger logger = LoggerFactory.getLogger(DiscountPolicyService.class);

    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo;
    private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

    public DiscountPolicyService(IAuthenticationService authenticationService,
                                 IProductionCompanyRepository productionCompanyRepository, IEventRepository eventRepo,
                                 IRepository<User> userRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }

    public Result<Boolean> createCompanyDiscountPolicy(String sessionToken, int companyID, DiscountPolicyRecord record) {
        return null;
    }

    public Result<Boolean> createEventDiscountPolicy(String sessionToken, int eventID, DiscountPolicyRecord record) {
        return null;
    }

    public Result<Boolean> editCompanyDiscountPolicy(String sessionToken, int companyID, DiscountPolicyRecord newRecord) {
        return null;
    }

    public Result<Boolean> editEventDiscountPolicy(String sessionToken, int eventID, DiscountPolicyRecord newRecord) {
        return null;
    }

    public Result<DiscountPolicyDTO> getEventDiscountPolicy(String sessionToken, int eventID) {
        return null;
    }

    public Result<DiscountPolicyDTO> getCompanyDiscountPolicy(String sessionToken, int companyID) {
        return null;
    }

    //applies coupon, changes the order price
    //return new order price
    //didnt include sessionToken as its not needed in the current system anymore
    //to verify the identity of the caller, use something similar to companyHierarchyService.isOwner
    //there it extracts the data from RequestContext, that includes the subjectID and its role
    //if you want sessionToken, add it here, and in the controller ethod found in EventDiscountPolicyController
    public Result<Double> applyCoupon(String orderID, String couponCode)
    {
        return null;
    }
}