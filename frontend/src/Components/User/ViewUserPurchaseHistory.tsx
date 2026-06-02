import { useEffect, useState } from "react";
import { useSession } from "../../App";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import ViewSaleHistory from "../ViewSaleHistory";

export default function UserPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");
  const { sessionToken } = useSession();

  useEffect(() => {
    async function loadUserPurchaseHistory() {
      try {
        const response = await fetch(
          "http://localhost:8080/api/user/me/order-history",
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: sessionToken,
            },
          },
        );

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
  }, []);

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      <ViewSaleHistory orders={orders} />
    </div>
  );
}
