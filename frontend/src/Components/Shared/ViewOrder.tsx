import { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import type { OrderDTO } from "../../DTOs/OrderDTO.tsx";
import "./CSS/ViewOrder.css";

type ViewOrderProps = {
  order?: OrderDTO;
  onClick?: () => void;
};

type LocationState = {
  order?: OrderDTO;
};

export default function ViewOrder({ order, onClick }: ViewOrderProps) {
  const { orderID } = useParams();
  const location = useLocation();
  const locationState = location.state as LocationState | null;

  const [error, setError] = useState<string>("");
  const [orderDTO, setOrderDTO] = useState<OrderDTO | null>(
    order ?? locationState?.order ?? null,
  );

  useEffect(() => {
    if (order) {
      setOrderDTO(order);
      return;
    }

    if (locationState?.order) {
      setOrderDTO(locationState.order);
      return;
    }

    if (!orderID) {
      return;
    }

    async function loadOrder() {
      try {
        const response = await fetch(`http://localhost:8080/orders/${orderID}`);

        if (!response.ok) {
          throw new Error(await response.text());
        }
        const order: OrderDTO = await response.json();

        setOrderDTO(order);
        /*const fakeOrderDTO: OrderDTO = {
          orderId: orderID,
          segmentId: "segmentA",
          numOfTickets: 2,
          orderType: "Seat",
          totalOrderPrice: 250.0,
          eventId: 1,
          subjectId: "42",
        };

        setOrderDTO(fakeOrderDTO);*/
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load order.");
      }
    }

    void loadOrder();
  }, [orderID, order, locationState?.order]);

  if (!orderDTO) {
    return <div className="loading">Loading order...</div>;
  }

  return (
    <div
      className={`view-order-container ${onClick ? "clickable-order" : ""}`}
      onClick={onClick}
    >
      {error && <p className="form-error">{error}</p>}

      <div className="card order-card">
        <h1 className="order-title">Order {orderDTO.orderId}</h1>

        <div className="order-row">
          <span className="order-label">Segment ID</span>
          <span className="order-value">{orderDTO.segmentId}</span>
        </div>

        <div className="order-row">
          <span className="order-label">Number of Tickets</span>
          <span className="order-value">{orderDTO.numOfTickets}</span>
        </div>

        <div className="order-row">
          <span className="order-label">Order Type</span>
          <span className="order-value">{orderDTO.orderType}</span>
        </div>

        <div className="order-row">
          <span className="order-label">Total Price</span>
          <span className="order-value">
            ₪{orderDTO.totalOrderPrice.toFixed(2)}
          </span>
        </div>

        <div className="order-row">
          <span className="order-label">Event ID</span>
          <span className="order-value">{orderDTO.eventId}</span>
        </div>

        <div className="order-row">
          <span className="order-label">Subject ID</span>
          <span className="order-value">{orderDTO.subjectId}</span>
        </div>
      </div>
    </div>
  );
}