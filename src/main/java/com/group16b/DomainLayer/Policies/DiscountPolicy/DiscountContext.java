package com.group16b.DomainLayer.Policies.DiscountPolicy;


import java.time.LocalDateTime;

public record DiscountContext(int age, int ticketCount, LocalDateTime date, String couponCode) {}