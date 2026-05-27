import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { OrderDTO } from "../DTOs/OrderDTO";

type ViewSaleHistoryProps = {
  orders: OrderDTO[];
};

export default function ViewSaleHistory({ orders }: ViewSaleHistoryProps) {
  const navigate = useNavigate();

  const [eventIdFilter, setEventIdFilter] = useState("");
  const [subjectIdFilter, setSubjectIdFilter] = useState("");

  const filteredOrders = orders.filter((order) => {
    const matchesEventId =
      eventIdFilter === "" || order.eventId.toString().includes(eventIdFilter);

    const matchesSubjectId =
      subjectIdFilter === "" || order.subjectId.includes(subjectIdFilter);

    return matchesEventId && matchesSubjectId;
  });

  function openEvent(eventId: number) {
    navigate(`/events/${eventId}`);
  }

  return (
    <div>
      <h1>Sale History</h1>

      <div>
        <label>
          Filter by Event ID:
          <input
            type="text"
            value={eventIdFilter}
            onChange={(e) => setEventIdFilter(e.target.value)}
          />
        </label>

        <label>
          Filter by Subject ID:
          <input
            type="text"
            value={subjectIdFilter}
            onChange={(e) => setSubjectIdFilter(e.target.value)}
          />
        </label>
      </div>

      {filteredOrders.length === 0 ? (
        <p>No orders found.</p>
      ) : (
        filteredOrders.map((order) => (
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
