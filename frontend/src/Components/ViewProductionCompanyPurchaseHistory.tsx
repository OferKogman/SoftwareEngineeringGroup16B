import { useEffect, useState } from "react";
import type { OrderDTO } from "../DTOs/OrderDTO";
import ViewSaleHistory from "./ViewSaleHistory";

export default function ProductionCompanyPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");
  const [eventIdFilter, setEventIdFilter] = useState<string>("");

  useEffect(() => {
    async function loadProductionCompanyPurchaseHistory() {
      try {
        // future backend call:
        // const ordersFromServer = await productionCompany.getSaleHistory();
        // setOrders(ordersFromServer);

        const mockOrders: OrderDTO[] = [
          {
            orderId: "ORD-2001",
            segmentId: "VIP-A",
            numOfTickets: 2,
            orderType: "Seat",
            totalOrderPrice: 500,
            eventId: 101,
            subjectId: "Ran123",
          },
          {
            orderId: "ORD-2002",
            segmentId: "Grass",
            numOfTickets: 4,
            orderType: "Field",
            totalOrderPrice: 800,
            eventId: 202,
            subjectId: "Ofer456",
          },
          {
            orderId: "ORD-2003",
            segmentId: "Front",
            numOfTickets: 1,
            orderType: "Seat",
            totalOrderPrice: 350,
            eventId: 101,
            subjectId: "Noa789",
          },
        ];

        setOrders(mockOrders);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load production company purchase history.",
        );
      }
    }

    void loadProductionCompanyPurchaseHistory();
  }, []);

  const filteredOrders = orders.filter((order) => {
    return (
      eventIdFilter === "" ||
      order.eventId.toString().includes(eventIdFilter)
    );
  });

  return (
    <div>
      <h1>Production Company Purchase History</h1>

      {error && <p className="form-error">{error}</p>}

      <div>
        <label>
          Filter by Event ID:{" "}
          <input
            type="text"
            value={eventIdFilter}
            onChange={(e) => setEventIdFilter(e.target.value)}
          />
        </label>
      </div>

      <ViewSaleHistory orders={filteredOrders} />
    </div>
  );
}