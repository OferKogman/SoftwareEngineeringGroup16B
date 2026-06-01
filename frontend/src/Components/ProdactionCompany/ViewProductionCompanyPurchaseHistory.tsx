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
      const authToken = localStorage.getItem("authToken") || "";

      try {
        // ====================================================
        // BACKEND VERSION
        //
        // CURRENTLY ACTIVE
        //
        // Uses:
        // GET /production-companies/{companyId}/sales-history
        //
        // If backend breaks during development,
        // comment this section and uncomment the MOCK section
        // below.
        // ====================================================
        /*
        const response = await fetch(
          `http://localhost:8080/production-companies/${productionCompanyID}/sales-history`,
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${authToken}`,
            },
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const orderList: OrderDTO[] = await response.json();

        setOrders(orderList);
        setError("");
        */

        // ====================================================
        // MOCK VERSION
        //
        // TO USE MOCK DATA:
        //
        // 1. Comment out the fetch code above
        // 2. Uncomment this block
        // ====================================================

        console.warn("====================================================");
        console.warn(
          "USING TEMPORARY MOCK DATA FOR ProductionCompanyPurchaseHistory",
        );
        console.warn("REMOVE THIS MOCK DATA BLOCK WHEN BACKEND IS READY");
        console.warn("====================================================");

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
        setError("");
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load production company purchase history.",
        );

        setOrders([]);
      }
    }

    void loadProductionCompanyPurchaseHistory();
  }, [productionCompanyID]);

  const filteredOrders = orders.filter((order) => {
    return (
      eventIdFilter === "" || order.eventId.toString().includes(eventIdFilter)
    );
  });

  return (
    <section className="production-company-history">
      <h1>Purchase History</h1>

      {error && <p className="form-error">{error}</p>}

      <div className="purchase-history-filter">
        <label className="filter-label">Filter by Event ID</label>

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
          <ViewOrder key={order.orderId} order={order} />
        ))}
      </div>
    </section>
  );
}
