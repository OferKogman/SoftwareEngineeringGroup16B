import type {
  NullablePurchasePolicyDTO,
  PurchasePolicyDTO,
} from "../../DTOs/PurchasePolicyDTO";
import "./CSS/ViewPurchasePolicies.css";

type Props = {
  purchasePolicy: NullablePurchasePolicyDTO;
};

export default function ViewPurchasePolicies({ purchasePolicy }: Props) {
  if (!purchasePolicy) {
    return <p>No purchase policy defined.</p>;
  }

  return <div className="purchase-policy">{renderPolicy(purchasePolicy)}</div>;
}

function renderPolicy(policy: PurchasePolicyDTO): React.ReactNode {
  if (!policy) return null;

  switch (policy.type) {
    case "AND":
      return (
        <div className="policy-box">
          <div className="policy-box-content">
            {renderPolicy(policy.left)}
            <div className="policy-box-title">AND</div>
            {renderPolicy(policy.right)}
          </div>
        </div>
      );

    case "OR":
      return (
        <div className="policy-box">
          <div className="policy-box-content">
            {renderPolicy(policy.left)}
            <div className="policy-box-title">OR</div>
            {renderPolicy(policy.right)}
          </div>
        </div>
      );

    case "MIN_AGE":
      return <div className="policy-leaf">Minimum Age: {policy.minAge}</div>;

    case "MAX_AGE":
      return <div className="policy-leaf">Maximum Age: {policy.maxAge}</div>;

    case "MIN_TICKETS":
      return (
        <div className="policy-leaf">Minimum Tickets: {policy.minTickets}</div>
      );

    case "MAX_TICKETS":
      return (
        <div className="policy-leaf">Maximum Tickets: {policy.maxTickets}</div>
      );
  }
}
