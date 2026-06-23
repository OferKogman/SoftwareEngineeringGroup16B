package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import java.time.LocalDateTime;

public class CouponCodeDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private String code;
    private LocalDateTime expirationDate;

    public CouponCodeDiscountDTO(double percentage, String code, LocalDateTime expirationDate) {
        super("Coupon Code");
        this.percentage = percentage;
        this.code = code;
        this.expirationDate = expirationDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
}