import { useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import ViewSaleHistory from "../ViewSaleHistory";

export default function UserPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");
  const { sessionToken } = useSession();

  const apiFetch = useApiFetch();

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
          const message = await response.text();
          throw new Error(message || "Failed to load user purchase history.");
        }

        const data: OrderDTO[] = await response.json();
        setOrders(data);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load user purchase history.",
        );
      }
    }

    void loadUserPurchaseHistory();
  }, [sessionToken, apiFetch]);

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      <ViewSaleHistory orders={orders} />
    </div>
  );
}
