
import type { PurchasePolicyDTO } from "../DTOs/PurchasePolicyDTO";

type ViewPurchasePoliciesProps = {
  purchasePolicy: PurchasePolicyDTO | null;
};

export default function ViewPurchasePolicies(
    { purchasePolicy }: ViewPurchasePoliciesProps ) {
        return (
            <div>
                {purchasePolicy ? (
                    <div>
                        {purchasePolicy.type === "Composite" && (
                            <div>
                                {purchasePolicy.leftPolicy && (
                                    <div>
                                        <ViewPurchasePolicies purchasePolicy={purchasePolicy.leftPolicy} />
                                    </div>
                                )}
                                <p>{purchasePolicy.operator}</p>
                                {purchasePolicy.rightPolicy && (
                                    <div>
                                        <ViewPurchasePolicies purchasePolicy={purchasePolicy.rightPolicy} />
                                    </div>
                                )}
                            </div>
                        )}
                        {purchasePolicy.type === "Minimum Age" && (
                            <p>Minimum Age: {purchasePolicy.minAge}</p>
                        )}
                        {purchasePolicy.type === "Maximum Age" && (
                            <p>Maximum Age: {purchasePolicy.maxAge}</p>
                        )}
                        {purchasePolicy.type === "Minimum Tickets Per Customer" && (
                            <p>Minimum Tickets: {purchasePolicy.minTickets}</p>
                        )}
                        {purchasePolicy.type === "Maximum Tickets Per Customer" && (
                            <p>Maximum Tickets: {purchasePolicy.maxTickets}</p>
                        )}
                        {purchasePolicy.type === "Lottery" && (
                            <div>
                                <p>Lottery Name: {purchasePolicy.lotteryName}</p>
                                <p>Winner Count: {purchasePolicy.lotteryWinnerCount}</p>
                                <p>Registration Due Date: {purchasePolicy.lotteryRegistrationDueDate}</p>
                            </div>
                        )}
                    </div>
                ) : (
                    <p>No purchase policy available.</p>
                )}
            </div>
        )

    }