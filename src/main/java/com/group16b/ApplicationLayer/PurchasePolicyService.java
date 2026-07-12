package com.group16b.ApplicationLayer;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.AndDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MaxAgeDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MaxTicketsDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MinAgeDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MinTicketsDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.OrDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.PurchasePolicyDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.PurchasePolicyRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.AgePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.AndPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MaxTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.OrPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;

@Service
@Transactional
public class PurchasePolicyService {

    private static final Logger logger =
            LoggerFactory.getLogger(PurchasePolicyService.class);

    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo;
    private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

    public PurchasePolicyService(
            IAuthenticationService authenticationService,
            IProductionCompanyRepository productionCompanyRepository,
            IEventRepository eventRepo,
            IRepository<User> userRepository) {

        this.authenticationService = authenticationService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }

    public Result<Boolean> createCompanyPurchasePolicy(
            String sessionToken,
            int companyID,
            PurchasePolicyRecord record) {

        return saveCompanyPolicy(
                sessionToken,
                companyID,
                record,
                false);
    }

    public Result<Boolean> editCompanyPurchasePolicy(
            String sessionToken,
            int companyID,
            PurchasePolicyRecord record) {

        return saveCompanyPolicy(
                sessionToken,
                companyID,
                record,
                true);
    }

    public Result<Boolean> createEventPurchasePolicy(
            String sessionToken,
            int eventID,
            PurchasePolicyRecord record) {

        return saveEventPolicy(
                sessionToken,
                eventID,
                record,
                false);
    }

    public Result<Boolean> editEventPurchasePolicy(
            String sessionToken,
            int eventID,
            PurchasePolicyRecord record) {

        return saveEventPolicy(
                sessionToken,
                eventID,
                record,
                true);
    }

    public Result<PurchasePolicyDTO> getEventPurchasePolicy(
            String sessionToken,
            int eventID) {

        try {
            validateUserAndGetID(sessionToken);

            Event event =
                    eventRepo.findByID(String.valueOf(eventID));

            return Result.makeOk(
                    toDTO(getSinglePolicy(
                            event.getEventPurchasePolicy())));

        } catch (Exception exception) {
            logger.warn(
                    "Failed to get event purchase policy: {}",
                    exception.getMessage());

            return Result.makeFail(exception.getMessage());
        }
    }

    public Result<PurchasePolicyDTO> getCompanyPurchasePolicy(
            String sessionToken,
            int companyID) {

        try {
            validateUserAndGetID(sessionToken);

            ProductionCompany company =
                    productionCompanyRepository.findByID(
                            String.valueOf(companyID));

            return Result.makeOk(
                    toDTO(getSinglePolicy(
                            company.getPurchasePolicy())));

        } catch (Exception exception) {
            logger.warn(
                    "Failed to get company purchase policy: {}",
                    exception.getMessage());

            return Result.makeFail(exception.getMessage());
        }
    }

    private Result<Boolean> saveCompanyPolicy(
            String sessionToken,
            int companyID,
            PurchasePolicyRecord record,
            boolean replace) {

        try {
            String userID =
                    validateUserAndGetID(sessionToken);

            ProductionCompany company =
                    productionCompanyRepository.findByID(
                            String.valueOf(companyID));

            company.validateUserPermissions(
                    userID,
                    ManagerPermissions.PURCHASE_POLICY);

            Set<PurchasePolicy> currentPolicies =
                    company.getPurchasePolicy();

            if (!replace && !currentPolicies.isEmpty()) {
                throw new IllegalStateException(
                        "Production company already has a purchase policy.");
            }

            if (!replace && record == null) {
                throw new IllegalArgumentException(
                        "Purchase policy cannot be null.");
            }

            for (PurchasePolicy policy : currentPolicies) {
                company.removePurchasePolicy(policy);
            }

            if (record != null) {
                company.addPurchasePolicy(
                        buildPolicy(record));
            }

            productionCompanyRepository.save(company);
            return Result.makeOk(true);

        } catch (Exception exception) {
            logger.warn(
                    "Failed to save company purchase policy: {}",
                    exception.getMessage());

            return Result.makeFail(exception.getMessage());
        }
    }

    private Result<Boolean> saveEventPolicy(
            String sessionToken,
            int eventID,
            PurchasePolicyRecord record,
            boolean replace) {

        try {
            String userID =
                    validateUserAndGetID(sessionToken);

            Event event =
                    eventRepo.findByID(
                            String.valueOf(eventID));

            ProductionCompany company =
                    productionCompanyRepository.findByID(
                            String.valueOf(
                                    event.getEventProductionCompanyID()));

            company.validateUserPermissions(
                    userID,
                    ManagerPermissions.PURCHASE_POLICY);

            Set<PurchasePolicy> currentPolicies =
                    event.getEventPurchasePolicy();

            if (!replace && !currentPolicies.isEmpty()) {
                throw new IllegalStateException(
                        "Event already has a purchase policy.");
            }

            if (!replace && record == null) {
                throw new IllegalArgumentException(
                        "Purchase policy cannot be null.");
            }

            for (PurchasePolicy policy : currentPolicies) {
                event.removeEventPurchasePolicy(policy);
            }

            if (record != null) {
                event.addEventPurchasePolicy(
                        buildPolicy(record));
            }

            eventRepo.save(event);
            return Result.makeOk(true);

        } catch (Exception exception) {
            logger.warn(
                    "Failed to save event purchase policy: {}",
                    exception.getMessage());

            return Result.makeFail(exception.getMessage());
        }
    }

    private String validateUserAndGetID(
            String sessionToken) {

        if (!authenticationService.validateToken(sessionToken)
                || !authenticationService.isUserToken(sessionToken)) {

            throw new IllegalArgumentException(
                    "Authentication failed. Please log in again.");
        }

        String userID =
                authenticationService.extractSubjectFromToken(
                        sessionToken);

        userRepository.findByID(userID);
        return userID;
    }

    private PurchasePolicy buildPolicy(
            PurchasePolicyRecord record) {

        if (record == null || record.type() == null) {
            throw new IllegalArgumentException(
                    "Purchase policy type is required.");
        }

        return switch (record.type()) {
            case MIN_AGE ->
                    new AgePolicy(
                            requireValue(
                                    record.minAge(),
                                    "Minimum age"),
                            null);

            case MAX_AGE ->
                    new AgePolicy(
                            null,
                            requireValue(
                                    record.maxAge(),
                                    "Maximum age"));

            case MIN_TICKETS ->
                    new MinTicketsPolicy(
                            requireValue(
                                    record.minTickets(),
                                    "Minimum tickets"));

            case MAX_TICKETS ->
                    new MaxTicketsPolicy(
                            requireValue(
                                    record.maxTickets(),
                                    "Maximum tickets"));

            case AND ->
                    new AndPolicy(
                            List.of(
                                    buildChild(
                                            record.left(),
                                            "AND left"),
                                    buildChild(
                                            record.right(),
                                            "AND right")));

            case OR ->
                    new OrPolicy(
                            List.of(
                                    buildChild(
                                            record.left(),
                                            "OR left"),
                                    buildChild(
                                            record.right(),
                                            "OR right")));
        };
    }

    private PurchasePolicy buildChild(
            PurchasePolicyRecord child,
            String name) {

        if (child == null) {
            throw new IllegalArgumentException(
                    name + " policy is required.");
        }

        return buildPolicy(child);
    }

    private int requireValue(
            Integer value,
            String name) {

        if (value == null) {
            throw new IllegalArgumentException(
                    name + " is required.");
        }

        return value;
    }

    private PurchasePolicy getSinglePolicy(
            Set<PurchasePolicy> policies) {

        if (policies == null || policies.isEmpty()) {
            return null;
        }

        return policies.iterator().next();
    }

    private PurchasePolicyDTO toDTO(
            PurchasePolicy policy) {

        if (policy == null) {
            return null;
        }

        if (policy instanceof AgePolicy agePolicy) {
            if (agePolicy.getMinAge() != null
                    && agePolicy.getMaxAge() != null) {

                return new AndDTO(
                        new MinAgeDTO(
                                agePolicy.getMinAge()),
                        new MaxAgeDTO(
                                agePolicy.getMaxAge()));
            }

            if (agePolicy.getMinAge() != null) {
                return new MinAgeDTO(
                        agePolicy.getMinAge());
            }

            return new MaxAgeDTO(
                    agePolicy.getMaxAge());
        }

        if (policy instanceof MinTicketsPolicy minPolicy) {
            return new MinTicketsDTO(
                    minPolicy.getMinTicketsPerTransaction());
        }

        if (policy instanceof MaxTicketsPolicy maxPolicy) {
            return new MaxTicketsDTO(
                    maxPolicy.getMaxTicketsPerTransaction());
        }

        if (policy instanceof AndPolicy andPolicy) {
            return combineDTOs(
                    andPolicy.getPolicies(),
                    true);
        }

        if (policy instanceof OrPolicy orPolicy) {
            return combineDTOs(
                    orPolicy.getPolicies(),
                    false);
        }

        throw new IllegalArgumentException(
                "Unsupported purchase policy type: "
                        + policy.getClass().getSimpleName());
    }

    private PurchasePolicyDTO combineDTOs(
            List<PurchasePolicy> policies,
            boolean and) {

        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException(
                    "Composite policy cannot be empty.");
        }

        PurchasePolicyDTO result =
                toDTO(policies.get(policies.size() - 1));

        for (int i = policies.size() - 2; i >= 0; i--) {
            PurchasePolicyDTO left =
                    toDTO(policies.get(i));

            result = and
                    ? new AndDTO(left, result)
                    : new OrDTO(left, result);
        }

        return result;
    }
}