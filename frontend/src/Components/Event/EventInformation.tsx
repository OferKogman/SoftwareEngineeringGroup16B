import { useNavigate } from "react-router-dom";
import ViewEvent from "./ViewEvent";

export default function EventInformation() {
  const navigate = useNavigate();

  return (
    <>
      <ViewEvent />

      <button
        className="order-tickets-button"
        onClick={() => navigate(`create-order`)}
      >
        Order Tickets
      </button>
    </>
  );
}
