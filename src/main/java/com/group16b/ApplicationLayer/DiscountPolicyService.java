package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicyDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.DiscountPolicy.AmountRangeDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.AndDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.CouponCodeDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DateRangeDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.DiscountPolicy.MaxDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.OrDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.SimpleDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.SumDiscount;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;

@Service
public class DiscountPolicyService {
    private static final Logger logger = LoggerFactory.getLogger(DiscountPolicyService.class);

    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo;
    private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

    public DiscountPolicyService(IAuthenticationService authenticationService,
                                 IProductionCompanyRepository productionCompanyRepository,
                                 IEventRepository eventRepo,
                                 IRepository<User> userRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }

    // ===================================================================
    // #368 — create discount policy for company
    // ===================================================================
    public Result<Boolean> createCompanyDiscountPolicy(String sessionToken, int companyID, DiscountPolicyRecord record) {
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
            company.addDiscountPolicy(buildPolicy(record));
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

    // ===================================================================
    // #370 — create discount policy for event
    // ===================================================================
    public Result<Boolean> createEventDiscountPolicy(String sessionToken, int eventID, DiscountPolicyRecord record) {
        try {
            logger.info("DiscountPolicyService.createEventDiscountPolicy: Received request for event ID: {}", eventID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            DiscountPolicy newPolicy = buildPolicy(record);

            while (true) {
                Event event = eventRepo.findByID(String.valueOf(eventID));
                ProductionCompany company = productionCompanyRepository
                        .findByID(String.valueOf(event.getEventProductionCompanyID()));
                company.validateUserPermissions(userID, ManagerPermissions.DISCOUNT_POLICY);
                event.addEventDiscountPolicy(newPolicy);
                try {
                    eventRepo.save(event);
                    break;
                } catch (OptimisticLockingFailureException e) {
                    logger.warn("DiscountPolicyService.createEventDiscountPolicy: Optimistic lock, retrying");
                }
            }

            logger.info("DiscountPolicyService.createEventDiscountPolicy: Policy added to event {} successfully", eventID);
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("DiscountPolicyService.createEventDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("DiscountPolicyService.createEventDiscountPolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ===================================================================
    // #369 — edit discount policy for company (replaces entire policy set)
    // ===================================================================
    public Result<Boolean> editCompanyDiscountPolicy(String sessionToken, int companyID, DiscountPolicyRecord newRecord) {
        try {
            logger.info("DiscountPolicyService.editCompanyDiscountPolicy: Received request for company ID: {}", companyID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            DiscountPolicy newPolicy = buildPolicy(newRecord);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            company.validateUserPermissions(userID, ManagerPermissions.DISCOUNT_POLICY);

            // Replace entire policy set with the new one
            for (DiscountPolicy dp : company.getDiscountPolicy()) {
                company.removeDiscountPolicy(dp);
            }
            company.addDiscountPolicy(newPolicy);
            productionCompanyRepository.save(company);

            logger.info("DiscountPolicyService.editCompanyDiscountPolicy: Policy replaced for company {} successfully", companyID);
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("DiscountPolicyService.editCompanyDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("DiscountPolicyService.editCompanyDiscountPolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ===================================================================
    // #371 — edit discount policy for event (replaces entire policy set)
    // ===================================================================
    public Result<Boolean> editEventDiscountPolicy(String sessionToken, int eventID, DiscountPolicyRecord newRecord) {
        try {
            logger.info("DiscountPolicyService.editEventDiscountPolicy: Received request for event ID: {}", eventID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            DiscountPolicy newPolicy = buildPolicy(newRecord);

            while (true) {
                Event event = eventRepo.findByID(String.valueOf(eventID));
                ProductionCompany company = productionCompanyRepository
                        .findByID(String.valueOf(event.getEventProductionCompanyID()));
                company.validateUserPermissions(userID, ManagerPermissions.DISCOUNT_POLICY);

                for (DiscountPolicy dp : event.getEventDiscountPolicy()) {
                    event.removeEventDiscountPolicy(dp);
                }
                event.addEventDiscountPolicy(newPolicy);
                try {
                    eventRepo.save(event);
                    break;
                } catch (OptimisticLockingFailureException e) {
                    logger.warn("DiscountPolicyService.editEventDiscountPolicy: Optimistic lock, retrying");
                }
            }

            logger.info("DiscountPolicyService.editEventDiscountPolicy: Policy replaced for event {} successfully", eventID);
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("DiscountPolicyService.editEventDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("DiscountPolicyService.editEventDiscountPolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ===================================================================
    // #372 — get event discount policy
    // ===================================================================
    public Result<DiscountPolicyDTO> getEventDiscountPolicy(String sessionToken, int eventID) {
        try {
            logger.info("DiscountPolicyService.getEventDiscountPolicy: Received request for event ID: {}", eventID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            Set<DiscountPolicy> policies = event.getEventDiscountPolicy();

            if (policies.isEmpty())
                return Result.makeOk(null);

            // Return the first (and typically only) policy as DTO
            DiscountPolicyDTO dto = new DiscountPolicyDTO(policies.iterator().next());
            logger.info("DiscountPolicyService.getEventDiscountPolicy: Retrieved policy for event {}", eventID);
            return Result.makeOk(dto);
        } catch (IllegalArgumentException e) {
            logger.error("DiscountPolicyService.getEventDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("DiscountPolicyService.getEventDiscountPolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ===================================================================
    // #373 — get company discount policy
    // ===================================================================
    public Result<DiscountPolicyDTO> getCompanyDiscountPolicy(String sessionToken, int companyID) {
        try {
            logger.info("DiscountPolicyService.getCompanyDiscountPolicy: Received request for company ID: {}", companyID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            Set<DiscountPolicy> policies = company.getDiscountPolicy();

            if (policies.isEmpty())
                return Result.makeOk(null);

            DiscountPolicyDTO dto = new DiscountPolicyDTO(policies.iterator().next());
            logger.info("DiscountPolicyService.getCompanyDiscountPolicy: Retrieved policy for company {}", companyID);
            return Result.makeOk(dto);
        } catch (IllegalArgumentException e) {
            logger.error("DiscountPolicyService.getCompanyDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("DiscountPolicyService.getCompanyDiscountPolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    // ===================================================================
    // FACTORY — recursive builder (And/Or/Sum/Max recurse into children)
    // ===================================================================
    private DiscountPolicy buildPolicy(DiscountPolicyRecord record) {
        if (record == null || record.type() == null)
            throw new IllegalArgumentException("Discount policy record and type must not be null.");
        return switch (record.type()) {
            case "SIMPLE" -> new SimpleDiscount(require(record));
            case "AMOUNT_RANGE" -> new AmountRangeDiscount(record.minTickets(), record.maxTickets(), require(record));
            case "DATE_RANGE" -> new DateRangeDiscount(record.startDate(), record.endDate(), require(record));
            case "COUPON" -> new CouponCodeDiscount(require(record), record.couponCode(), record.expiryDate(), record.maxUsages());
            case "AND" -> new AndDiscount(buildChildren(record), require(record));
            case "OR"  -> new OrDiscount(buildChildren(record), require(record));
            case "SUM" -> new SumDiscount(buildChildren(record));
            case "MAX" -> new MaxDiscount(buildChildren(record));
            default -> throw new IllegalArgumentException("Unknown discount type: " + record.type());
        };
    }

    private List<DiscountPolicy> buildChildren(DiscountPolicyRecord record) {
        if (record.children() == null || record.children().isEmpty())
            throw new IllegalArgumentException("Composite type '" + record.type() + "' must have at least one child.");
        List<DiscountPolicy> children = new ArrayList<>();
        for (DiscountPolicyRecord child : record.children()) {
            children.add(buildPolicy(child));
        }
        return children;
    }

    private double require(DiscountPolicyRecord record) {
        if (record.discountPercentage() == null)
            throw new IllegalArgumentException("Type '" + record.type() + "' requires a discountPercentage.");
        return record.discountPercentage();
    }
}