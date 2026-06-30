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

  return "";
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

  function closePopup() {
    setError("");
  }

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
          setError(error instanceof Error ? error.message : "");
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
    <div className="revenue-page">
      <div className="revenue-view">
        <h2 className="revenue-title">Total Revenue</h2>

        {loading ? (
          <div className="revenue-content">Loading revenue...</div>
        ) : (
          <div className="revenue-content">
            {error && (
              <div className="settings-alert">
                <p>{error}</p>
                <button onClick={closePopup}> OK </button>
              </div>
            )}

            <p className="revenue-label">Total Revenue Generated</p>

            <h1 className="revenue-value">
              ₪ {(revenue ?? 0).toLocaleString()}
            </h1>

            <p className="revenue-company">Company ID: {companyId}</p>
          </div>
        )}
      </div>
    </div>
  );
}
