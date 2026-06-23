package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public class CompositeDiscountDTO extends DiscountPolicyDTO {
    private DiscountPolicyDTO leftPolicy;
    private DiscountPolicyDTO rightPolicy;
    private String operator;

    public CompositeDiscountDTO(DiscountPolicyDTO leftPolicy, DiscountPolicyDTO rightPolicy, String operator) {
        super("Composite");
        this.leftPolicy = leftPolicy;
        this.rightPolicy = rightPolicy;
        this.operator = operator;
    }

    public DiscountPolicyDTO getLeftPolicy() {
        return leftPolicy;
    }

    public DiscountPolicyDTO getRightPolicy() {
        return rightPolicy;
    }

    public String getOperator() {
        return operator;
    }
}