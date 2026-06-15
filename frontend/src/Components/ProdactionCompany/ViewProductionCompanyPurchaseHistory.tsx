import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { OrderDTO } from "../../DTOs/OrderDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import ViewOrder from "../Shared/ViewOrder";
import "./CSS/ViewProductionCompanyPurchaseHistory.css";

const API_BASE = "http://localhost:8080";

function getApiError(data: unknown): string {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && typeof data === "object") {
    if ("message" in data && typeof data.message === "string") {
      return data.message;
    }

    if ("error" in data && typeof data.error === "string") {
      return data.error;
    }
  }

  return "Failed to load production company purchase history.";
}

async function readResponseBody(response: Response): Promise<unknown> {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function isOrderList(data: unknown): data is OrderDTO[] {
  return Array.isArray(data);
}

export default function ProductionCompanyPurchaseHistory() {
  const { companyId } = useParams();
  const { sessionToken } = useSession();

  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventIdFilter, setEventIdFilter] = useState("");

  const apiFetch = useApiFetch();

  useEffect(() => {
    let cancelled = false;

    async function loadProductionCompanyPurchaseHistory() {
      setLoading(true);
      setError("");
      setOrders([]);

      if (!companyId) {
        setError("Missing company ID.");
        setLoading(false);
        return;
      }

      if (!sessionToken) {
        return;
      }

      try {
        const response = await apiFetch(
          `${API_BASE}/production-companies/${companyId}/sales-history`,
          {
            method: "GET",
          },
        );

        const data = await readResponseBody(response);

        if (cancelled) {
          return;
        }

        if (!response.ok) {
          throw new Error(getApiError(data));
        }

        if (!isOrderList(data)) {
          throw new Error("Invalid purchase history response from server.");
        }

        setOrders(data);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Failed to load production company purchase history.",
          );
          setOrders([]);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadProductionCompanyPurchaseHistory();

    return () => {
      cancelled = true;
    };
  }, [companyId, sessionToken, apiFetch]);

  const filteredOrders = orders.filter((order) => {
    return (
      eventIdFilter === "" || order.eventId.toString().includes(eventIdFilter)
    );
  });

  return (
    <section className="production-company-history">
      <h1>Purchase History</h1>

      {loading && <p>Loading purchase history...</p>}
      {error && <p className="form-error">{error}</p>}

      {!loading && !error && (
        <>
          <div className="purchase-history-filter">
            <label className="filter-label">Filter by Event ID</label>

            <input
              className="filter-input"
              type="text"
              placeholder="Search by Event ID..."
              value={eventIdFilter}
              onChange={(e) => setEventIdFilter(e.target.value)}
            />
          </div>

          <div className="orders-list">
            {filteredOrders.length > 0 ? (
              filteredOrders.map((order) => (
                <ViewOrder key={order.orderId} order={order} />
              ))
            ) : (
              <p>No purchases found.</p>
            )}
          </div>
        </>
      )}
    </section>
  );
}
