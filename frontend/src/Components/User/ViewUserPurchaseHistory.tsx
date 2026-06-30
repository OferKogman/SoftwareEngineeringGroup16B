import { useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import ViewOrder from "../Shared/ViewOrder";
import "./CSS/ViewUserPurchaseHistory.css";

export default function UserPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");
  const { sessionToken } = useSession();

  const apiFetch = useApiFetch();

  function closePopup() {
    setError("");
  }

  useEffect(() => {
    async function loadUserPurchaseHistory() {
      if (!sessionToken) {
        setError("Missing session token.");
        setOrders([]);
        return;
      }
      try {
        const response = await apiFetch(
          "http://localhost:8080/api/user/me/order-history",
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const data: OrderDTO[] = await response.json();
        setOrders(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
      }
    }

    void loadUserPurchaseHistory();
  }, [sessionToken, apiFetch]);

  return (
    <div>
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}

      <div className="orders-list">
        {orders.length > 0 ? (
          orders.map((order) => <ViewOrder key={order.orderId} order={order} />)
        ) : (
          <p>No purchases found.</p>
        )}
      </div>
    </div>
  );
}
