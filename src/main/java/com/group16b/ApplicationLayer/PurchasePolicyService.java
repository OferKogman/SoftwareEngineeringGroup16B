package com.group16b.ApplicationLayer;

import java.util.ArrayList;
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
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MaxTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.OrPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.TicketAmountPolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;

@Service
@Transactional
public class PurchasePolicyService {
    private static final Logger logger = LoggerFactory.getLogger(PurchasePolicyService.class);

    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo;
    private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

    public PurchasePolicyService(IAuthenticationService authenticationService,
                                 IProductionCompanyRepository productionCompanyRepository,
                                 IEventRepository eventRepo,
                                 IRepository<User> userRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }

    public Result<Boolean> createCompanyPurchasePolicy(String sessionToken, int companyID,
                                                       PurchasePolicyRecord record) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            company.addPurchasePolicy(buildPolicy(record));
            productionCompanyRepository.save(company);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("PurchasePolicyService.createCompanyPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<Boolean> createEventPurchasePolicy(String sessionToken, int eventID,
                                                     PurchasePolicyRecord record) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            ProductionCompany company = productionCompanyRepository
                    .findByID(String.valueOf(event.getEventProductionCompanyID()));

            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            event.addEventPurchasePolicy(buildPolicy(record));
            eventRepo.save(event);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("PurchasePolicyService.createEventPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<Boolean> editCompanyPurchasePolicy(String sessionToken, int companyID,
                                                     PurchasePolicyRecord newRecord) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            company.setPurchasePolicies(Set.of(buildPolicy(newRecord)));
            productionCompanyRepository.save(company);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("PurchasePolicyService.editCompanyPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<Boolean> editEventPurchasePolicy(String sessionToken, int eventID,
                                                   PurchasePolicyRecord newRecord) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            ProductionCompany company = productionCompanyRepository
                    .findByID(String.valueOf(event.getEventProductionCompanyID()));

            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            event.setEventPurchasePolicies(Set.of(buildPolicy(newRecord)));
            eventRepo.save(event);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("PurchasePolicyService.editEventPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<PurchasePolicyDTO> getEventPurchasePolicy(String sessionToken, int eventID) {
        try {
            validateUserAndGetId(sessionToken);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            return Result.makeOk(toDTO(combinePolicies(event.getEventPurchasePolicy())));
        } catch (Exception e) {
            logger.error("PurchasePolicyService.getEventPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<PurchasePolicyDTO> getCompanyPurchasePolicy(String sessionToken, int companyID) {
        try {
            validateUserAndGetId(sessionToken);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            return Result.makeOk(toDTO(combinePolicies(company.getPurchasePolicy())));
        } catch (Exception e) {
            logger.error("PurchasePolicyService.getCompanyPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    private String validateUserAndGetId(String sessionToken) {
        if (!authenticationService.validateToken(sessionToken)
                || !authenticationService.isUserToken(sessionToken)) {
            throw new IllegalArgumentException("Authentication failed. Please log in again.");
        }

        String userID = authenticationService.extractSubjectFromToken(sessionToken);
        userRepository.findByID(userID);
        return userID;
    }

    private PurchasePolicy buildPolicy(PurchasePolicyRecord record) {
        if (record == null || record.type() == null) {
            throw new IllegalArgumentException("Purchase policy type is required.");
        }

        return switch (record.type()) {
            case MIN_AGE -> new AgePolicy(require(record.minAge(), "minAge"), null);
            case MAX_AGE -> new AgePolicy(null, require(record.maxAge(), "maxAge"));
            case MIN_TICKETS -> new MinTicketsPolicy(require(record.minTickets(), "minTickets"));
            case MAX_TICKETS -> new MaxTicketsPolicy(require(record.maxTickets(), "maxTickets"));
            case AND -> new AndPolicy(List.of(buildPolicy(record.left()), buildPolicy(record.right())));
            case OR -> new OrPolicy(List.of(buildPolicy(record.left()), buildPolicy(record.right())));
        };
    }

    private int require(Integer value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }

    private PurchasePolicy combinePolicies(Set<PurchasePolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            return null;
        }

        List<PurchasePolicy> list = new ArrayList<>(policies.stream()
                .filter(policy -> !(policy instanceof LotteryPolicy))
                .toList());

        if (list.isEmpty()) {
            return null;
        }

        if (list.size() == 1) {
            return list.get(0);
        }

        return new AndPolicy(list);
    }

    private PurchasePolicyDTO toDTO(PurchasePolicy policy) {
        if (policy == null) {
            return null;
        }

        if (policy instanceof AgePolicy agePolicy) {
            if (agePolicy.getMinAge() != null) {
                return new MinAgeDTO(agePolicy.getMinAge());
            }
            return new MaxAgeDTO(agePolicy.getMaxAge());
        }

        if (policy instanceof MinTicketsPolicy minTicketsPolicy) {
            return new MinTicketsDTO(minTicketsPolicy.getMinTicketsPerTransaction());
        }

        if (policy instanceof MaxTicketsPolicy maxTicketsPolicy) {
            return new MaxTicketsDTO(maxTicketsPolicy.getMaxTicketsPerTransaction());
        }

        if (policy instanceof TicketAmountPolicy ticketAmountPolicy) {
            if (ticketAmountPolicy.getMinTickets() != null
                    && ticketAmountPolicy.getMaxTickets() != null) {
                return new AndDTO(
                        new MinTicketsDTO(ticketAmountPolicy.getMinTickets()),
                        new MaxTicketsDTO(ticketAmountPolicy.getMaxTickets()));
            }

            if (ticketAmountPolicy.getMinTickets() != null) {
                return new MinTicketsDTO(ticketAmountPolicy.getMinTickets());
            }

            return new MaxTicketsDTO(ticketAmountPolicy.getMaxTickets());
        }

        if (policy instanceof AndPolicy andPolicy) {
            return toBinaryDTO(andPolicy.getPolicies(), true);
        }

        if (policy instanceof OrPolicy orPolicy) {
            return toBinaryDTO(orPolicy.getPolicies(), false);
        }

        throw new IllegalArgumentException("Unsupported purchase policy type: "
                + policy.getClass().getSimpleName());
    }

    private PurchasePolicyDTO toBinaryDTO(List<PurchasePolicy> policies, boolean and) {
        if (policies == null || policies.isEmpty()) {
            return null;
        }

        PurchasePolicyDTO result = toDTO(policies.get(0));

        for (int i = 1; i < policies.size(); i++) {
            result = and
                    ? new AndDTO(result, toDTO(policies.get(i)))
                    : new OrDTO(result, toDTO(policies.get(i)));
        }

        return result;
    }
}