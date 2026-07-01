package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.AndDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.CouponDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.DiscountPolicyDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MaxDateDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MaxDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MaxTicketsDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MinDateDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MinTicketsDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.OrDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.SimpleDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.SumDiscountDTO;
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
@Transactional
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

    public Result<Boolean> createCompanyDiscountPolicy(String sessionToken, int companyID,
                                                       DiscountPolicyRecord record) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            company.addDiscountPolicy(buildPolicy(record));
            productionCompanyRepository.save(company);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("DiscountPolicyService.createCompanyDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<Boolean> createEventDiscountPolicy(String sessionToken, int eventID,
                                                     DiscountPolicyRecord record) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            ProductionCompany company = productionCompanyRepository
                    .findByID(String.valueOf(event.getEventProductionCompanyID()));

            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            event.addEventDiscountPolicy(buildPolicy(record));
            eventRepo.save(event);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("DiscountPolicyService.createEventDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<Boolean> editCompanyDiscountPolicy(String sessionToken, int companyID,
                                                     DiscountPolicyRecord newRecord) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            if (newRecord == null) {
                company.setDiscountPolicies(Set.of());
            } else {
                company.setDiscountPolicies(Set.of(buildPolicy(newRecord)));
            }

            productionCompanyRepository.save(company);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("DiscountPolicyService.editCompanyDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<Boolean> editEventDiscountPolicy(String sessionToken, int eventID,
                                                   DiscountPolicyRecord newRecord) {
        try {
            String userID = validateUserAndGetId(sessionToken);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            ProductionCompany company = productionCompanyRepository
                    .findByID(String.valueOf(event.getEventProductionCompanyID()));

            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            if (newRecord == null) {
                event.setEventDiscountPolicies(Set.of());
            } else {
                event.setEventDiscountPolicies(Set.of(buildPolicy(newRecord)));
            }

            eventRepo.save(event);

            return Result.makeOk(true);
        } catch (Exception e) {
            logger.error("DiscountPolicyService.editEventDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<DiscountPolicyDTO> getEventDiscountPolicy(String sessionToken, int eventID) {
        try {
            validateUserAndGetId(sessionToken);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            return Result.makeOk(toDTO(combinePolicies(event.getEventDiscountPolicy())));
        } catch (Exception e) {
            logger.error("DiscountPolicyService.getEventDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<DiscountPolicyDTO> getCompanyDiscountPolicy(String sessionToken, int companyID) {
        try {
            validateUserAndGetId(sessionToken);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            return Result.makeOk(toDTO(combinePolicies(company.getDiscountPolicy())));
        } catch (Exception e) {
            logger.error("DiscountPolicyService.getCompanyDiscountPolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<Double> applyCoupon(String orderID, String couponCode) {
        return Result.makeFail("Coupon discounts are not implemented yet.");
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

    private DiscountPolicy buildPolicy(DiscountPolicyRecord record) {
        if (record == null || record.type() == null) {
            throw new IllegalArgumentException("Discount policy type is required.");
        }

        return switch (record.type()) {
            case SIMPLE -> new SimpleDiscount(requireDouble(record.percentage(), "percentage"));

            case MIN_TICKETS -> new AmountRangeDiscount(
                    requireInt(record.minAmount(), "minAmount"),
                    null,
                    requireDouble(record.percentage(), "percentage"));

            case MAX_TICKETS -> new AmountRangeDiscount(
                    null,
                    requireInt(record.maxAmount(), "maxAmount"),
                    requireDouble(record.percentage(), "percentage"));

            case MIN_DATE -> new DateRangeDiscount(
                    requireDate(record.startDate(), "startDate"),
                    null,
                    requireDouble(record.percentage(), "percentage"));

            case MAX_DATE -> new DateRangeDiscount(
                    null,
                    requireDate(record.endDate(), "endDate"),
                    requireDouble(record.percentage(), "percentage"));

            case AND -> new AndDiscount(
                    buildPolicy(record.left()),
                    buildPolicy(record.right()),
                    requireDouble(record.percentage(), "percentage"));

            case OR -> new OrDiscount(
                    buildPolicy(record.left()),
                    buildPolicy(record.right()),
                    requireDouble(record.percentage(), "percentage"));

            case SUM -> new SumDiscount(
                    buildPolicy(record.left()),
                    buildPolicy(record.right()));

            case MAX -> new MaxDiscount(
                    buildPolicy(record.left()),
                    buildPolicy(record.right()));

            case COUPON -> throw new IllegalArgumentException(
                    "Coupon discounts are not implemented in the discount policy tree.");
        };
    }

    private DiscountPolicy combinePolicies(Set<DiscountPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            return null;
        }

        List<DiscountPolicy> list = new ArrayList<>(policies);

        if (list.size() == 1) {
            return list.get(0);
        }

        DiscountPolicy combined = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            combined = new SumDiscount(combined, list.get(i));
        }

        return combined;
    }

    private DiscountPolicyDTO toDTO(DiscountPolicy policy) {
        if (policy == null) {
            return null;
        }

        if (policy instanceof SimpleDiscount simpleDiscount) {
            return new SimpleDiscountDTO(simpleDiscount.getDiscountPercentage());
        }

        if (policy instanceof AmountRangeDiscount amountRangeDiscount) {
            if (amountRangeDiscount.getMinTickets() != null) {
                return new MinTicketsDiscountDTO(
                        amountRangeDiscount.getDiscountPercentage(),
                        amountRangeDiscount.getMinTickets());
            }

            return new MaxTicketsDiscountDTO(
                    amountRangeDiscount.getDiscountPercentage(),
                    amountRangeDiscount.getMaxTickets());
        }

        if (policy instanceof DateRangeDiscount dateRangeDiscount) {
            if (dateRangeDiscount.getStartDate() != null) {
                return new MinDateDiscountDTO(
                        dateRangeDiscount.getDiscountPercentage(),
                        dateRangeDiscount.getStartDate());
            }

            return new MaxDateDiscountDTO(
                    dateRangeDiscount.getDiscountPercentage(),
                    dateRangeDiscount.getEndDate());
        }

        if (policy instanceof AndDiscount andDiscount) {
            return new AndDiscountDTO(
                    toDTO(andDiscount.getLeft()),
                    toDTO(andDiscount.getRight()),
                    andDiscount.getDiscountPercentage());
        }

        if (policy instanceof OrDiscount orDiscount) {
            return new OrDiscountDTO(
                    toDTO(orDiscount.getLeft()),
                    toDTO(orDiscount.getRight()),
                    orDiscount.getDiscountPercentage());
        }

        if (policy instanceof SumDiscount sumDiscount) {
            if (sumDiscount.getRight() == null) {
                return toDTO(sumDiscount.getLeft());
            }

            return new SumDiscountDTO(
                    toDTO(sumDiscount.getLeft()),
                    toDTO(sumDiscount.getRight()));
        }

        if (policy instanceof MaxDiscount maxDiscount) {
            if (maxDiscount.getRight() == null) {
                return toDTO(maxDiscount.getLeft());
            }

            return new MaxDiscountDTO(
                    toDTO(maxDiscount.getLeft()),
                    toDTO(maxDiscount.getRight()));
        }

        if (policy instanceof CouponCodeDiscount couponDiscount) {
            return new CouponDiscountDTO(
                    couponDiscount.getDiscountPercentage(),
                    couponDiscount.getCode(),
                    couponDiscount.getExpiryDate());
        }

        throw new IllegalArgumentException("Unsupported discount policy type: "
                + policy.getClass().getSimpleName());
    }

    private int requireInt(Integer value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }

    private double requireDouble(Double value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }

    private LocalDateTime requireDate(LocalDateTime value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value;
    }
}