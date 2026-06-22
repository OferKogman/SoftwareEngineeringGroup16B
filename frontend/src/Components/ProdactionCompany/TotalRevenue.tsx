import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import { useSession } from "../../GlobalContext/SessionContext";
import "./CSS/TotalRevenue.css";

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

  return "Could not load total revenue";
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

function getRevenue(data: unknown): number {
  if (typeof data === "number") {
    return data;
  }

  if (data && typeof data === "object" && "data" in data) {
    const value = data.data;

    if (typeof value === "number") {
      return value;
    }

    if (typeof value === "string") {
      const parsedValue = Number(value);

      if (!Number.isNaN(parsedValue)) {
        return parsedValue;
      }
    }
  }

  throw new Error("Invalid total revenue response from server");
}

export default function TotalRevenue() {
  const { companyId } = useParams();
  const { sessionToken } = useSession();

  const [revenue, setRevenue] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const apiFetch = useApiFetch();

  useEffect(() => {
    let cancelled = false;

    async function loadRevenue() {
      setLoading(true);
      setError("");
      setRevenue(null);

      if (!companyId) {
        setError("Missing company id");
        setLoading(false);
        return;
      }

      if (!sessionToken) {
        return;
      }

      try {
        const response = await apiFetch(
          `${API_BASE}/production-companies/${companyId}/total-revenue`,
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

        setRevenue(getRevenue(data));
      } catch (error) {
        if (!cancelled) {
          setError(
            error instanceof Error
              ? error.message
              : "Could not load total revenue",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadRevenue();

    return () => {
      cancelled = true;
    };
  }, [companyId, sessionToken, apiFetch]);

  return (
    <div className="total-revenue-page">
      <h2 className="total-revenue-title">Total Revenue</h2>

      {loading ? (
        <div className="total-revenue-card">
          <div className="total-revenue-label">Loading revenue...</div>
        </div>
      ) : error ? (
        <div className="total-revenue-card">
          <div className="total-revenue-label">Could not load revenue</div>
          <p className="form-error">{error}</p>
        </div>
      ) : (
        <>
          <div className="total-revenue-card">
            <div className="total-revenue-label">Total Revenue Generated</div>

            <h1 className="total-revenue-value">
              ₪ {(revenue ?? 0).toLocaleString()}
            </h1>

            <div className="total-revenue-company">Company ID: {companyId}</div>
          </div>

          <div className="revenue-stats-grid">
            <div className="revenue-stat-card">
              <h3>Completed Orders</h3>
              <p>438</p>
            </div>

            <div className="revenue-stat-card">
              <h3>Events</h3>
              <p>12</p>
            </div>

            <div className="revenue-stat-card">
              <h3>Average Order</h3>
              <p>₪286</p>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
