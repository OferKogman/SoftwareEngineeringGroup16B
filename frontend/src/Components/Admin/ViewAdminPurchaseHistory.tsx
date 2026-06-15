import { useCallback, useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import ViewSaleHistory from "../ViewSaleHistory";

export default function AdminPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");

  type AdminHistoryMode = "all" | "byUser" | "byCompany";

  const apiFetch = useApiFetch();

  const [selectedMode, setSelectedMode] = useState<AdminHistoryMode>("all");
  const [idInput, setIdInput] = useState<string>("");

  const loadAllPurchaseHistory = useCallback(async () => {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/viewAllPurchasesHistory`,
        {
          method: "GET",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to load purchase history.");
      }

      const ordersFromServer: OrderDTO[] = await response.json();

      setOrders(ordersFromServer);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to load all purchase history.",
      );
    }
  }, [apiFetch]);

  useEffect(() => {
    const loadPurchaseHistory = async () => {
      await loadAllPurchaseHistory();
    };

    void loadPurchaseHistory();
  }, [loadAllPurchaseHistory]);

  async function loadSelectedPurchaseHistory() {
    if (selectedMode === "all") {
      await loadAllPurchaseHistory();
      return;
    }

    if (selectedMode === "byCompany") {
      await loadByProductionCompany();
      return;
    }

    await loadByUserId();
  }
  async function loadByProductionCompany() {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/viewPurchasesHistoryByCompany/${idInput}`,
        {
          method: "GET",
        },
      );
      if (!response.ok) {
        throw new Error(
          "Failed to load purchase history by production company.",
        );
      }
      const ordersFromServer: OrderDTO[] = await response.json();
      setOrders(ordersFromServer);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to load purchase history by production company.",
      );
    }
  }

  async function loadByUserId() {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/viewPurchasesHistoryByUser/${idInput}`,
        {
          method: "GET",
        },
      );
      if (!response.ok) {
        throw new Error("Failed to load purchase history by user.");
      }
      const ordersFromServer: OrderDTO[] = await response.json();
      setOrders(ordersFromServer);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to load purchase history by user.",
      );
    }
  }

  return (
    <div>
      <h1>Admin Purchase History</h1>

      {error && <p className="form-error">{error}</p>}

      <div>
        <label>
          Choose history type:{" "}
          <select
            value={selectedMode}
            onChange={(e) => {
              setSelectedMode(e.target.value as AdminHistoryMode);
              setIdInput("");
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
              type="text"
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
            />
          </label>
        </div>
      )}

      <button onClick={loadSelectedPurchaseHistory}>Load</button>

      <ViewSaleHistory orders={orders} />
    </div>
  );
}
