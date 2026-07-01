package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.AndDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.DiscountPolicyDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MaxDateDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MaxDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MaxTicketsDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MinDateDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MinTicketsDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.OrDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.SimpleDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.SumDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.AndDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MaxAgeDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MaxTicketsDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MinAgeDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MinTicketsDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.OrDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.PurchasePolicyDTO;
import com.group16b.DomainLayer.Event.Event;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.CouponDiscountDTO;
import com.group16b.DomainLayer.Policies.DiscountPolicy.AmountRangeDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.AndDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.CouponCodeDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DateRangeDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.DiscountPolicy.MaxDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.OrDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.SimpleDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.SumDiscount;

import com.group16b.DomainLayer.Policies.PurchasePolicy.AgePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.AndPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.DefaultPurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MaxTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.OrPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.TicketAmountPolicy;


public class EventDTO {
	private final int eventID;
	private boolean active = false;
	private String venueID;
	private String name;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String artist;
	private String category;
	private final int productionCompanyID;
	private final DiscountPolicyDTO discountPolicy;
	private final PurchasePolicyDTO purchasePolicy;
	private double price;
	private double rating;

	private LotteryDTO lotteryDTO;

	public EventDTO(Event event) {
		eventID = event.getEventID();
		active = event.getEventStatus();
		venueID = event.getEventVenueID();
		name = event.getEventName();
		startTime = event.getEventStartTime();
		endTime = event.getEventEndTime();
		artist = event.getEventArtist();
		category = event.getEventCategory();
		productionCompanyID = event.getEventProductionCompanyID();
		discountPolicy = toDiscountDTO(combineDiscountPolicies(event.getEventDiscountPolicy()));
		purchasePolicy = toPurchaseDTO(combinePurchasePolicies(event.getEventPurchasePolicy()));
		price = event.getEventPrice();
		rating = event.getEventRating();

		lotteryDTO = event.hasLotteryPolicy() ? new LotteryDTO(event.getLotteryPolicy()) : null;
	}

	public int getEventID() {
		return this.eventID;
	}

	public boolean getEventStatus() {
		return this.active;
	}

	public String getEventVenueID() {
		return this.venueID;
	}

	public String getEventName() {
		return this.name;
	}

	public LocalDateTime getEventStartTime() {
		return this.startTime;
	}

	public LocalDateTime getEventEndTime() {
		return this.endTime;
	}

	public String getEventArtist() {
		return this.artist;
	}

	public String getEventCategory() {
		return this.category;
	}

	public int getEventProductionCompanyID() {
		return this.productionCompanyID;
	}

	public DiscountPolicyDTO getEventDiscountPolicy() {
		return this.discountPolicy;
	}

	public PurchasePolicyDTO getEventPurchasePolicy() {
		return this.purchasePolicy;
	}

	public double getEventPrice() {
		return this.price;
	}

	public double getEventRating() {
		return this.rating;
	}

	public LotteryDTO getLotteryDTO() {
		return this.lotteryDTO;
	}

	private DiscountPolicy combineDiscountPolicies(Set<DiscountPolicy> policies) {
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

	private DiscountPolicyDTO toDiscountDTO(DiscountPolicy policy) {
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
					toDiscountDTO(andDiscount.getLeft()),
					toDiscountDTO(andDiscount.getRight()),
					andDiscount.getDiscountPercentage());
		}

		if (policy instanceof OrDiscount orDiscount) {
			return new OrDiscountDTO(
					toDiscountDTO(orDiscount.getLeft()),
					toDiscountDTO(orDiscount.getRight()),
					orDiscount.getDiscountPercentage());
		}

		if (policy instanceof SumDiscount sumDiscount) {
			if (sumDiscount.getRight() == null) {
				return toDiscountDTO(sumDiscount.getLeft());
			}

			return new SumDiscountDTO(
					toDiscountDTO(sumDiscount.getLeft()),
					toDiscountDTO(sumDiscount.getRight()));
		}

		if (policy instanceof MaxDiscount maxDiscount) {
			if (maxDiscount.getRight() == null) {
				return toDiscountDTO(maxDiscount.getLeft());
			}

			return new MaxDiscountDTO(
					toDiscountDTO(maxDiscount.getLeft()),
					toDiscountDTO(maxDiscount.getRight()));
		}

		if (policy instanceof CouponCodeDiscount couponDiscount) {
			return new CouponDiscountDTO(
					couponDiscount.getDiscountPercentage(),
					couponDiscount.getCode(),
					couponDiscount.getExpiryDate());
		}

		return null;
	}

	private PurchasePolicy combinePurchasePolicies(Set<PurchasePolicy> policies) {
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

	private PurchasePolicyDTO toPurchaseDTO(PurchasePolicy policy) {
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

		if (policy instanceof DefaultPurchasePolicy defaultPolicy) {
			return new AndDTO(
					new AndDTO(
							new MinTicketsDTO(defaultPolicy.getMinTicketsPerTransaction()),
							new MaxTicketsDTO(defaultPolicy.getMaxTicketsPerTransaction())),
					new MinAgeDTO(defaultPolicy.getMinAge()));
		}

		if (policy instanceof AndPolicy andPolicy) {
			return toBinaryPurchaseDTO(andPolicy.getPolicies(), true);
		}

		if (policy instanceof OrPolicy orPolicy) {
			return toBinaryPurchaseDTO(orPolicy.getPolicies(), false);
		}

		return null;
	}

	private PurchasePolicyDTO toBinaryPurchaseDTO(List<PurchasePolicy> policies, boolean and) {
		if (policies == null || policies.isEmpty()) {
			return null;
		}

		PurchasePolicyDTO result = toPurchaseDTO(policies.get(0));

		for (int i = 1; i < policies.size(); i++) {
			result = and
					? new AndDTO(result, toPurchaseDTO(policies.get(i)))
					: new OrDTO(result, toPurchaseDTO(policies.get(i)));
		}

		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof EventDTO)) {
			return false;
		}
		EventDTO event = (EventDTO) o;
		return eventID == event.eventID;
	}
}
