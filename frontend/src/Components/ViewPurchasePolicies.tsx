import type { PurchasePolicyDTO } from "../DTOs/PurchasePolicyDTO";

type PurchasePolicyProps = {
  purchasePolicy: PurchasePolicyDTO | null;
};

export default function ViewPurchasePolicies({ purchasePolicy }: PurchasePolicyProps) {
  if (!purchasePolicy) {
    return <span>No purchase policy.</span>;
  }

  return (
    <InnerPurchasePolicy purchasePolicy={purchasePolicy} isRoot={true} />
  );
}

function InnerPurchasePolicy({
  purchasePolicy,
  isRoot,
}: {
  purchasePolicy: PurchasePolicyDTO;
  isRoot: boolean;
}) {
  // LEAF
  if (purchasePolicy.type !== "Composite") {
    return (
      <span>
        {purchasePolicy.type === "Minimum Age" &&
          `Minimum Age: ${purchasePolicy.minAge}`}

        {purchasePolicy.type === "Maximum Age" &&
          `Maximum Age: ${purchasePolicy.maxAge}`}

        {purchasePolicy.type === "Minimum Tickets Per Customer" &&
          `Minimum Tickets: ${purchasePolicy.minTickets}`}

        {purchasePolicy.type === "Maximum Tickets Per Customer" &&
          `Maximum Tickets: ${purchasePolicy.maxTickets}`}

        {purchasePolicy.type === "Lottery" && (
          <span>
            Lottery Name: {purchasePolicy.lotteryName}{" "}
            {isLotteryOpen(purchasePolicy.lotteryRegistrationDueDate)}
          </span>
        )}
      </span>
    );
  }

  // COMPOSITE
  const content = (
    <span>
      {purchasePolicy.leftPolicy && (
        <InnerPurchasePolicy
          purchasePolicy={purchasePolicy.leftPolicy}
          isRoot={false}
        />
      )}

      {" "}{purchasePolicy.operator}{" "}

      {purchasePolicy.rightPolicy && (
        <InnerPurchasePolicy
          purchasePolicy={purchasePolicy.rightPolicy}
          isRoot={false}
        />
      )}
    </span>
  );

  // put parentheses if needed in inner structure
  return isRoot ? content : <span>( {content} )</span>;
}


const isLotteryOpen = (dateStr: string) => {
  const dueDate = new Date(dateStr);
  const now = new Date();

  return now <= dueDate ? "OPEN" : "CLOSED";
};