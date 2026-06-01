import { useEffect, useState } from "react";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import ViewOrder from "../Shered/ViewOrder";
import "./CSS/ViewProductionCompanyPurchaseHistory.css";

export default function ProductionCompanyPurchaseHistory() {

  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");
  const [eventIdFilter, setEventIdFilter] = useState<string>("");

  useEffect(() => {
    async function loadProductionCompanyPurchaseHistory() {
      try {
        const response = await fetch(`http://localhost:8080/ProductionCompanyService.viewSalesHistory/${productionCompanyID}`);

        if (!response.ok) {
          throw new Error(await response.text());
        }
        const OrderList: OrderDTO[] = await response.json();

        setOrders(OrderList);
        /*const mockOrders: OrderDTO[] = [
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

        setOrders(mockOrders);*/
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

      <div className="purchase-history-filter">
  <label className="filter-label">
  Filter by Event ID
</label>

<input
  className="filter-input"
  type="text"
  placeholder="Search by Event ID..."
    value={eventIdFilter}
    onChange={(e) => setEventIdFilter(e.target.value)}
  />
</div>

      <div className="orders-list">
        {filteredOrders.map((order) => (
          <ViewOrder
            key={order.orderId}
            order={order}
          />
        ))}
      </div>
    </div>
  );
}