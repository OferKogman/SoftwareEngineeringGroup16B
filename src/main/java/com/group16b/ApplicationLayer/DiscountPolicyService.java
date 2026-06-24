package com.group16b.ApplicationLayer;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.*;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Policies.DiscountPolicy.*;

@Service
public class DiscountPolicyService {
    private static final Logger logger = LoggerFactory.getLogger(DiscountPolicyService.class);

    private final IAuthenticationService authenticationService;
    private final IProductionCompanyRepository productionCompanyRepository;
    private final IEventRepository eventRepository;
    private final IRepository<User> userRepository;

    public DiscountPolicyService(IAuthenticationService authenticationService,
                                 IProductionCompanyRepository productionCompanyRepository,
                                 IRepository<User> userRepository, IEventRepository eventRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    // ===== #368: UNIFIED SET METHOD (create or edit) =====

    public Result<DiscountPolicyDTO> setCompanyDiscountPolicy(String sessionToken, int companyID, DiscountPolicyDTO policyDTO) {
        try {
            logger.info("DiscountPolicyService.setCompanyDiscountPolicy: Setting policy for company {}", companyID);

            authenticationService.validateToken(sessionToken);  // validate first
            String userID = authenticationService.extractSubjectFromToken(sessionToken);  // then extract userID
            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));  // convert int to String
            if (company == null) {
                return Result.makeFail("Company not found");
            }

            DiscountPolicy domainPolicy = buildPolicy(policyDTO);
            company.setDiscountPolicy(domainPolicy);
            productionCompanyRepository.save(company);

            logger.info("DiscountPolicyService.setCompanyDiscountPolicy: Policy set for company {}", companyID);

            DiscountPolicyDTO resultDTO = toDTO(domainPolicy);
            return Result.makeOk(resultDTO);

        } catch (Exception e) {
            logger.error("DiscountPolicyService.setCompanyDiscountPolicy: Error: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
    }

    public Result<DiscountPolicyDTO> setEventDiscountPolicy(String sessionToken, int eventID, DiscountPolicyDTO policyDTO) {
        try {
            logger.info("DiscountPolicyService.setEventDiscountPolicy: Setting policy for event {}", eventID);

            authenticationService.validateToken(sessionToken);  // validate first
            String userID = authenticationService.extractSubjectFromToken(sessionToken);  // then extract userID
            Event event = eventRepository.findByID(String.valueOf(eventID));  // convert int to String
            if (event == null) {
                return Result.makeFail("Event not found");
            }

            DiscountPolicy domainPolicy = buildPolicy(policyDTO);
            event.setEventDiscountPolicy(domainPolicy);
            eventRepository.save(event);

            logger.info("DiscountPolicyService.setEventDiscountPolicy: Policy set for event {}", eventID);

            DiscountPolicyDTO resultDTO = toDTO(domainPolicy);
            return Result.makeOk(resultDTO);

        } catch (Exception e) {
            logger.error("DiscountPolicyService.setEventDiscountPolicy: Error: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
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
        try {
            // Validate token
            if (!authenticationService.validateToken(sessionToken)) {
                return Result.makeFail("Authentication failed: Invalid Token");
            }

            authenticationService.extractSubjectFromToken(sessionToken);

            // Find company
            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

            // Get discount policy
            DiscountPolicy policy = company.getDiscountPolicy();

            // Convert to DTO if policy exists, else return null
            if (policy == null) {
                return Result.makeOk(null);
            }

            DiscountPolicyDTO dto = toDTO(policy);
            return Result.makeOk(dto);

        } catch (IllegalArgumentException e) {
            return Result.makeFail("Illegal argument: " + e.getMessage());
        } catch (Exception e) {
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Double> applyCoupon(String orderID, String couponCode) {
        return null;
    }

    

    private DiscountPolicy buildPolicy(DiscountPolicyDTO dto) {
        if (dto instanceof RegularDiscountDTO) {
            RegularDiscountDTO d = (RegularDiscountDTO) dto;
            return new SimpleDiscount(d.getPercentage());
        }
        else if (dto instanceof MinimumPurchaseDiscountDTO) {
            MinimumPurchaseDiscountDTO d = (MinimumPurchaseDiscountDTO) dto;
            return new AmountRangeDiscount(d.getMinimumAmount(), null, d.getPercentage());
        }
        else if (dto instanceof MaximumPurchaseDiscountDTO) {
            MaximumPurchaseDiscountDTO d = (MaximumPurchaseDiscountDTO) dto;
            return new AmountRangeDiscount(null, d.getMaximumAmount(), d.getPercentage());
        }
        else if (dto instanceof EarlyBirdDiscountDTO) {
            EarlyBirdDiscountDTO d = (EarlyBirdDiscountDTO) dto;
            return new DateRangeDiscount(null, d.getEarlyBirdEndDate(), d.getPercentage());
        }
        else if (dto instanceof LastMinuteDiscountDTO) {
            LastMinuteDiscountDTO d = (LastMinuteDiscountDTO) dto;
            return new DateRangeDiscount(d.getLastMinuteStartDate(), null, d.getPercentage());
        }
        else if (dto instanceof CouponCodeDiscountDTO) {
            CouponCodeDiscountDTO d = (CouponCodeDiscountDTO) dto;
            return new CouponCodeDiscount(d.getPercentage(), d.getCode(), d.getExpirationDate(), null);
        }
        else if (dto instanceof CompositeDiscountDTO) {
            CompositeDiscountDTO d = (CompositeDiscountDTO) dto;
            DiscountPolicy left = buildPolicy(d.getLeftPolicy());
            DiscountPolicy right = buildPolicy(d.getRightPolicy());

            switch (d.getOperator()) {
                case "AND":
                    return new AndDiscount(left, right, 0);
                case "OR":
                    return new OrDiscount(left, right, 0);
                case "SUM":
                    return new SumDiscount(left, right);
                case "MAX":
                    return new MaxDiscount(left, right);
                default:
                    throw new IllegalArgumentException("Unknown operator: " + d.getOperator());
            }
        }
        throw new IllegalArgumentException("Unknown DTO type");
    }

    private DiscountPolicyDTO toDTO(DiscountPolicy policy) {
        if (policy instanceof SimpleDiscount) {
            SimpleDiscount s = (SimpleDiscount) policy;
            return new RegularDiscountDTO(s.getDiscountPercentage());
        }
        else if (policy instanceof AmountRangeDiscount) {
            AmountRangeDiscount a = (AmountRangeDiscount) policy;
            if (a.getMinTickets() != null && a.getMaxTickets() == null) {
                return new MinimumPurchaseDiscountDTO(a.getDiscountPercentage(), a.getMinTickets());
            } else if (a.getMinTickets() == null && a.getMaxTickets() != null) {
                return new MaximumPurchaseDiscountDTO(a.getDiscountPercentage(), a.getMaxTickets());
            }
        }
        else if (policy instanceof DateRangeDiscount) {
            DateRangeDiscount d = (DateRangeDiscount) policy;
            if (d.getStartDate() == null && d.getEndDate() != null) {
                return new EarlyBirdDiscountDTO(d.getDiscountPercentage(), d.getEndDate());
            } else if (d.getStartDate() != null && d.getEndDate() == null) {
                return new LastMinuteDiscountDTO(d.getDiscountPercentage(), d.getStartDate());
            }
        }
        else if (policy instanceof CouponCodeDiscount) {
            CouponCodeDiscount c = (CouponCodeDiscount) policy;
            return new CouponCodeDiscountDTO(c.getDiscountPercentage(), c.getCode(), c.getExpiryDate());
        }
        else if (policy instanceof AndDiscount) {
            AndDiscount and = (AndDiscount) policy;
            return new CompositeDiscountDTO("AND", toDTO(and.getLeft()), toDTO(and.getRight()));
        }
        else if (policy instanceof OrDiscount) {
            OrDiscount or = (OrDiscount) policy;
            return new CompositeDiscountDTO("OR", toDTO(or.getLeft()), toDTO(or.getRight()));
        }
        else if (policy instanceof SumDiscount) {
            SumDiscount sum = (SumDiscount) policy;
            return new CompositeDiscountDTO("SUM", toDTO(sum.getLeft()), toDTO(sum.getRight()));
        }
        else if (policy instanceof MaxDiscount) {
            MaxDiscount max = (MaxDiscount) policy;
            return new CompositeDiscountDTO("MAX", toDTO(max.getLeft()), toDTO(max.getRight()));
        }
        throw new IllegalArgumentException("Unknown policy type");
    }
}