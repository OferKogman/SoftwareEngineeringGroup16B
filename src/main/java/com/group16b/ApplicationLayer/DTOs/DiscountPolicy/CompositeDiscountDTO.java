package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public class CompositeDiscountDTO extends DiscountPolicyDTO {
    private DiscountPolicyDTO leftPolicy;
    private DiscountPolicyDTO rightPolicy;
    private String operator; // "AND", "OR", "SUM", "MAX"

    public CompositeDiscountDTO(String operator, DiscountPolicyDTO leftPolicy, DiscountPolicyDTO rightPolicy) {
        super("Composite");
        this.operator = operator;
        this.leftPolicy = leftPolicy;
        this.rightPolicy = rightPolicy;
    }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public DiscountPolicyDTO getLeftPolicy() { return leftPolicy; }
    public void setLeftPolicy(DiscountPolicyDTO leftPolicy) { this.leftPolicy = leftPolicy; }
    public DiscountPolicyDTO getRightPolicy() { return rightPolicy; }
    public void setRightPolicy(DiscountPolicyDTO rightPolicy) { this.rightPolicy = rightPolicy; }
}