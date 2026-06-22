import { useNavigate } from "react-router-dom";
import type { OrderDTO } from "../../DTOs/OrderDTO";

type ViewSaleHistoryProps = {
  orders: OrderDTO[];
};

export default function ViewSaleHistory({ orders }: ViewSaleHistoryProps) {
  const navigate = useNavigate();

  function openEvent(eventId: number) {
    navigate(`/events/${eventId}`);
  }

  function openOrder(orderId: string) {
    navigate(`/orders/${orderId}`);
  }

  return (
    <div>
      {orders.length === 0 ? (
        <p>No orders found.</p>
      ) : (
        orders.map((order) => (
          <div key={order.orderId}>
            <h4>
              Order:{" "}
              <button onClick={() => openOrder(order.orderId)}>
                {order.orderId}
              </button>
            </h4>

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
