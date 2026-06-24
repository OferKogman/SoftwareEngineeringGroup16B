package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import java.time.LocalDateTime;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class CouponDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private String code;
    private LocalDateTime expirationDate;

    public CouponDiscountDTO(double percentage, String code, LocalDateTime expirationDate) {
        super(DiscountPolicyTypes.COUPON);
        this.percentage = percentage;
        this.code = code;
        this.expirationDate = expirationDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
}