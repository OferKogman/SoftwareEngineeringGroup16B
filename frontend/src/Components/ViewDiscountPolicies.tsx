import type { DiscountPolicyDTO } from "../DTOs/DiscountPolicyDTO";

type DiscountPolicyProps = {
  discountPolicy: DiscountPolicyDTO | null;
};

export default function ViewDiscountPolicies({ discountPolicy }: DiscountPolicyProps) {
    if (!discountPolicy) { 
        return <span>No discount policy.</span>;
    }
  // LEAF
  if (discountPolicy && discountPolicy.type !== "Composite") {
    return (
      <span>
        {discountPolicy.type === "Regular" && `Discount: ${discountPolicy.percentage}%`}
        {discountPolicy.type === "Early Bird" && `Discount: ${discountPolicy.percentage}% until ${new Date(discountPolicy.earlyBirdEndDate).toLocaleDateString()}`}
        {discountPolicy.type === "Last Minute" &&
          `Discount: ${discountPolicy.percentage}% from ${new Date(discountPolicy.lastMinuteStartDate).toLocaleDateString()}`}
        {discountPolicy.type === "Minimum Purchase" &&
          `Discount: ${discountPolicy.percentage}% from minimum tickets: $${discountPolicy.minimumAmount.toFixed(2)})`}
        {discountPolicy.type === "Maximum Purchase" &&
          `Discount: ${discountPolicy.percentage}% up to maximum tickets: $${discountPolicy.maximumAmount.toFixed(2)})`}
        {discountPolicy.type === "Coupon Code" &&
          (<span>
            Coupon Code: {discountPolicy.code}{" "}
            Discount Percentage: {discountPolicy.percentage}%
            Expiration Date: {new Date(discountPolicy.expirationDate).toLocaleDateString()}
          </span>)}
      </span>
    );
  }

  // COMPOSITE
  return (
    <span>
      ({" "}

      {discountPolicy.leftPolicy && (
        <ViewDiscountPolicies discountPolicy={discountPolicy.leftPolicy} />
      )}

      {" "}{discountPolicy.operator}{" "}

      {discountPolicy.rightPolicy && (
        <ViewDiscountPolicies discountPolicy={discountPolicy.rightPolicy} />
      )}

      {" "})
    </span>
  );
}