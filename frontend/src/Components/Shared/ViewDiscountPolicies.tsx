import type {
  DiscountPolicyDTO,
  NullableDiscountPolicyDTO,
} from "../../DTOs/DiscountPolicyDTO";
import "./CSS/ViewPurchasePolicies.css";

type Props = {
  discountPolicy: NullableDiscountPolicyDTO;
};

export default function ViewDiscountPolicies({ discountPolicy }: Props) {
  if (!discountPolicy) {
    return <p>No discount policy defined.</p>;
  }

  return <div className="purchase-policy">{renderPolicy(discountPolicy)}</div>;
}

function renderPolicy(
  policy: DiscountPolicyDTO,
  hideDiscountLine: boolean = false,
): React.ReactNode {
  if (!policy) return null;

  switch (policy.type) {
    case "AND":
      return (
        <div className="policy-box">
          <div className="policy-box-content">
            {renderPolicy(policy.left, true)}
            <div className="policy-box-title">AND ({policy.percentage}%)</div>
            {renderPolicy(policy.right, true)}
          </div>
        </div>
      );

    case "OR":
      return (
        <div className="policy-box">
          <div className="policy-box-content">
            {renderPolicy(policy.left, true)}
            <div className="policy-box-title">OR ({policy.percentage}%)</div>
            {renderPolicy(policy.right, true)}
          </div>
        </div>
      );

    case "SUM":
      return (
        <div className="policy-box">
          <div className="policy-box-content">
            {renderPolicy(policy.left)}
            <div className="policy-box-title">SUM</div>
            {renderPolicy(policy.right)}
          </div>
        </div>
      );

    case "MAX":
      return (
        <div className="policy-box">
          <div className="policy-box-content">
            {renderPolicy(policy.left)}
            <div className="policy-box-title">MAX</div>
            {renderPolicy(policy.right)}
          </div>
        </div>
      );

    case "SIMPLE":
      return hideDiscountLine ? null : (
        <div className="policy-leaf">Discount: {policy.percentage}%</div>
      );

    case "MIN_DATE":
      return (
        <div className="policy-leaf">
          Minimum Date: {new Date(policy.startDate).toLocaleDateString()}
          {!hideDiscountLine && (
            <>
              <br />
              Discount: {policy.percentage}%
            </>
          )}
        </div>
      );

    case "MAX_DATE":
      return (
        <div className="policy-leaf">
          Maximum Date: {new Date(policy.endDate).toLocaleDateString()}
          {!hideDiscountLine && (
            <>
              <br />
              Discount: {policy.percentage}%
            </>
          )}
        </div>
      );

    case "MIN_TICKETS":
      return (
        <div className="policy-leaf">
          Minimum Tickets: {policy.minAmount}
          {!hideDiscountLine && (
            <>
              <br />
              Discount: {policy.percentage}%
            </>
          )}
        </div>
      );

    case "MAX_TICKETS":
      return (
        <div className="policy-leaf">
          Maximum Tickets: {policy.maxAmount}
          {!hideDiscountLine && (
            <>
              <br />
              Discount: {policy.percentage}%
            </>
          )}
        </div>
      );
  }
}
