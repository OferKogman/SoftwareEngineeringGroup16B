import { useEffect, useState } from "react";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import ViewSaleHistory from "../ViewSaleHistory";

export default function AdminPurchaseHistory() {
  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [error, setError] = useState<string>("");

  type AdminHistoryMode = "all" | "byUser" | "byCompany";

  const [selectedMode, setSelectedMode] = useState<AdminHistoryMode>("all");
  const [idInput, setIdInput] = useState<string>("");

  useEffect(() => {
    void loadAllPurchaseHistory();
  }, []);

  async function loadAllPurchaseHistory() {
    try {
      const response = await fetch(
        `http://localhost:8080/viewAllPurchasesHistory`,
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
  }
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
      setError("");

      // future backend call:
      // const ordersFromServer = await admin.getByProductionCompany(idInput);
      //       // setOrders(ordersFromServer);

      const mockOrders: OrderDTO[] = [
        {
          orderId: "ORD-PC-1",
          segmentId: "VIP-A",
          numOfTickets: 3,
          orderType: "Seat",
          totalOrderPrice: 750,
          eventId: 404,
          subjectId: "CompanyUser1",
        },
        {
          orderId: "ORD-PC-2",
          segmentId: "Grass",
          numOfTickets: 5,
          orderType: "Field",
          totalOrderPrice: 1000,
          eventId: 505,
          subjectId: "CompanyUser2",
        },
      ];

      setOrders(mockOrders);
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
      setError("");

      // future backend call:
      // const ordersFromServer = await admin.getByUserId(idInput);
      // setOrders(ordersFromServer);

      const mockOrders: OrderDTO[] = [
        {
          orderId: "ORD-USER-1",
          segmentId: "Front",
          numOfTickets: 1,
          orderType: "Seat",
          totalOrderPrice: 350,
          eventId: 101,
          subjectId: idInput || "MockUser",
        },
        {
          orderId: "ORD-USER-2",
          segmentId: "Grass",
          numOfTickets: 2,
          orderType: "Field",
          totalOrderPrice: 400,
          eventId: 202,
          subjectId: idInput || "MockUser",
        },
      ];

      setOrders(mockOrders);
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
