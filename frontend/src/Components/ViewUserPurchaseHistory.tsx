import { useEffect, useState } from "react";
import type { OrderDTO } from "../DTOs/OrderDTO";
import ViewSaleHistory from "./ViewSaleHistory";

export default function UserPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    async function loadUserPurchaseHistory() {
      try {
        // future backend call:
        // const ordersFromServer = await user.getSaleHistory();
        // setOrders(ordersFromServer);

        const mockOrders: OrderDTO[] = [
          {
            orderId: "ORD-1001",
            segmentId: "VIP-A",
            numOfTickets: 2,
            orderType: "Seat",
            totalOrderPrice: 500,
            eventId: 101,
            subjectId: "Ran123",
          },
          {
            orderId: "ORD-1003",
            segmentId: "Front",
            numOfTickets: 1,
            orderType: "Seat",
            totalOrderPrice: 350,
            eventId: 101,
            subjectId: "Ran123",
          },
        ];

        setOrders(mockOrders);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load user purchase history.",
        );
      }
    }

    void loadUserPurchaseHistory();
  }, []);

  return (
    <div>
      <h1>My Purchase History</h1>

      {error && <p className="form-error">{error}</p>}

      <ViewSaleHistory orders={orders} />
    </div>
  );
}