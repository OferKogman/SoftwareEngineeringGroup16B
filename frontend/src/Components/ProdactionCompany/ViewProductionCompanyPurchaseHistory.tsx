import { useEffect, useState } from "react";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import ViewOrder from "../Shared/ViewOrder";
import "./CSS/ViewProductionCompanyPurchaseHistory.css";

type ProductionCompanyPurchaseHistoryProps = {
  productionCompanyID: string;
};

export default function ProductionCompanyPurchaseHistory({
  productionCompanyID,
}: ProductionCompanyPurchaseHistoryProps) {

  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");
  const [eventIdFilter, setEventIdFilter] = useState<string>("");

  useEffect(() => {
  async function loadProductionCompanyPurchaseHistory() {
    /*try {

      const response = await fetch(
        `http://localhost:8080/production-companies/${productionCompanyID}/sales-history`,
        {
          method: "GET",
          headers: {
            Authorization: authToken,
          },
        }
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const orderList: OrderDTO[] = await response.json();
      setOrders(orderList);
      setError("");
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to load production company purchase history."
      );*/

      console.warn("====================================================");
      console.warn("USING TEMPORARY MOCK DATA FOR ProductionCompanyPurchaseHistory");
      console.warn("REMOVE THIS MOCK DATA BLOCK WHEN BACKEND IS READY");
      console.warn("====================================================");

      // ====================================================
      // TEMPORARY MOCK DATA
      //
      // This block exists ONLY so the UI can be developed
      // before the backend endpoint is fully connected.
      //
      // REMOVE THIS ENTIRE BLOCK once the backend endpoint
      // /production-companies/{companyId}/sales-history
      // returns real data.
      // ====================================================
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
    }
  //}

  void loadProductionCompanyPurchaseHistory();
}, [productionCompanyID]);

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