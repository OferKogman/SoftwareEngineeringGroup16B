import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { OrderDTO } from "../DTOs/OrderDTO.tsx";

export default function ViewOrder() {
  const { orderID } = useParams();
  const [error, setError] = useState<string>("");
  const [orderDTO, setOrderDTO] = useState<OrderDTO | null>(null);

  useEffect(() => {
    if (!orderID) {
      return;
    }

    async function loadOrder() {
      try {
        // const response = await fetch(`/api/orders/${orderID}`);

        // if (!response.ok) {
        //   throw new Error("Failed to load order.");
        // }

        // const order: OrderDTO = await response.json();

        const fakeOrderDTO: OrderDTO = {
          orderID: "order1",
          segmentId: "segmentA",
          numOfTickets: 2,
          orderType: "Seat",
          tocalOrderPrice: 250.0,
          eventId: 1,
          subjectId: "42",
        };

        setOrderDTO(fakeOrderDTO);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load order.");
      }
    }

    void loadOrder();
  }, [orderID]);

  if (!orderDTO) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      <h1>Order {orderDTO.orderID}</h1>

      <p>Segment ID: {orderDTO.segmentId}</p>

      <p>Number of Tickets: {orderDTO.numOfTickets}</p>

      <p>Order Type: {orderDTO.orderType}</p>

      <p>Total Order Price: {orderDTO.tocalOrderPrice}</p>

      <p>Event ID: {orderDTO.eventId}</p>

      <p>Subject ID: {orderDTO.subjectId}</p>
    </div>
  );
}