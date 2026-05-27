import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { OrderDTO } from "../DTOs/OrderDTO";

export default function ViewSaleHistory() {
  const navigate = useNavigate();

  const [error, setError] = useState<string>("");
  const [orders, setOrders] = useState<OrderDTO[] | null>(null);


  useEffect(() => {
    async function loadOrders() {
      try {
        // future backend fetch here

        /*
        const response = await fetch("/api/orders/history");

        if (!response.ok) {
          throw new Error("Failed to load orders.");
        }

        const ordersFromServer: OrderDTO[] = await response.json();

        setOrders(ordersFromServer);
        */

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
            orderId: "ORD-1002",
            segmentId: "Grass",
            numOfTickets: 4,
            orderType: "Field",
            totalOrderPrice: 800,
            eventId: 202,
            subjectId: "Ofer456",
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
          err instanceof Error ? err.message : "Failed to load orders.",
        );
      }
    }

    void loadOrders();
  }, []);

  function openEvent(eventId: number) {
    navigate(`/events/${eventId}`);
  }

  if (!orders) {
    return <div>No orders found.</div>;
  }



  return (
    <div>
      <h1>Sale History</h1>

      {error && <p className="form-error">{error}</p>}

      

      {orders.length === 0 ? (
        <p>No orders found.</p>
      ) : (
        orders.map((order) => (
          <div key={order.orderId}>
            <h3>Order {order.orderId}</h3>

            <p>
              Event ID:{" "}
              <button onClick={() => openEvent(order.eventId)}>
                {order.eventId}
              </button>
            </p>

            <p>Subject ID: {order.subjectId}</p>
            <p>Segment ID: {order.segmentId}</p>
            <p>Number of Tickets: {order.numOfTickets}</p>
            <p>Order Type: {order.orderType}</p>
            <p>Total Order Price: {order.totalOrderPrice}</p>
          </div>
        ))
      )}
    </div>
  );
}