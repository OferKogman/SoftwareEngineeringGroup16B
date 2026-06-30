import { useCallback, useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import ViewOrder from "../Shared/ViewOrder";
import "./CSS/ViewAdminPurchaseHistory.css";

export default function AdminPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");
  const [message, setMessage] = useState<string>("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  type AdminHistoryMode = "all" | "byUser" | "byCompany";

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  const [selectedMode, setSelectedMode] = useState<AdminHistoryMode>("all");
  const [idInput, setIdInput] = useState<string | number>("");

  const loadAllPurchaseHistory = useCallback(async () => {
    setMessage("");
    setError("");

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/viewAllPurchasesHistory`,
        {
          method: "GET",
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const ordersFromServer: OrderDTO[] = await response.json();

      setOrders(ordersFromServer);
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsLoading(false);
    }
  }, [apiFetch]);

  useEffect(() => {
    const loadPurchaseHistory = async () => {
      await loadAllPurchaseHistory();
    };

    void loadPurchaseHistory();
  }, [loadAllPurchaseHistory]);

  async function loadSelectedPurchaseHistory() {
    setIsSubmitting(true);
    setMessage("");
    setError("");

    try {
      if (selectedMode === "all") {
        await loadAllPurchaseHistory();
        return;
      }

      if (selectedMode === "byCompany") {
        await loadByProductionCompany();
        return;
      }

      await loadByUserId();
    } finally {
      setIsSubmitting(false);
    }
  }
  async function loadByProductionCompany() {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/viewPurchesHistoryByCompany/${idInput}`,
        {
          method: "GET",
        },
      );
      if (!response.ok) {
        throw new Error(await response.text());
      }
      const ordersFromServer: OrderDTO[] = await response.json();
      setOrders(ordersFromServer);
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    }
  }

  async function loadByUserId() {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/viewPurchesHistoryByUser/${idInput}`,
        {
          method: "GET",
        },
      );
      if (!response.ok) {
        throw new Error(await response.text());
      }
      const ordersFromServer: OrderDTO[] = await response.json();
      setOrders(ordersFromServer);
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    }
  }

  if (isLoading) {
    return <p>Loading purchase history...</p>;
  }

  return (
    <div>
      <h1>Admin Purchase History</h1>

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}

      <div>
        <label>
          Choose history type:{" "}
          <select
            value={selectedMode}
            onChange={(e) => {
              const mode = e.target.value as AdminHistoryMode;
              setSelectedMode(mode);
              setIdInput(mode === "byUser" ? "" : 0);
            }}
          >
            <option value="all">All purchase history</option>
            <option value="byUser">By user ID</option>
            <option value="byCompany">By production company ID</option>
          </select>
        </label>
      </div>

      {selectedMode !== "all" && (
        <div>
          <label>
            {selectedMode === "byUser"
              ? "User ID: "
              : "Production Company ID: "}
            <input
              type={selectedMode === "byUser" ? "mail" : "number"}
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
            />
          </label>
        </div>
      )}

      <button disabled={isSubmitting} onClick={loadSelectedPurchaseHistory}>
        {isSubmitting ? "Loading..." : "Load"}
      </button>

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
