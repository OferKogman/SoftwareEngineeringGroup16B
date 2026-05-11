package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public abstract class CouponDiscount extends Discount{
    private String code;
    private LocalDateTime expiryDate;

    protected CouponDiscount(double discountPercentage, double discountAmount, String code, LocalDateTime expiryDate) {
        super(discountPercentage, discountAmount);
        if(expiryDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Discount is expired");
        }
        if(code == null){
            throw new IllegalArgumentException("Null code");
        }
        if(code.isEmpty()){
            throw new IllegalArgumentException("Empty code");
        }
        this.code = code;
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        if(expiryDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Discount is expired");
        }
        this.expiryDate = expiryDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if(code == null){
            throw new IllegalArgumentException("Null code");
        }
        if(code.isEmpty()){
            throw new IllegalArgumentException("Empty code");
        }
        this.code = code;
    }
}
