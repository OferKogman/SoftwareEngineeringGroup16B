import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import "../../CSS/Management.css";
import type { EventDTO } from "../../DTOs/EventDTO";

const API_BASE = "http://localhost:8080";

/* ================= SAFE RESPONSE PARSER ================= */
async function readResponseBody(response: Response): Promise<unknown> {
  const text = await response.text();

  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text; // fallback for plain text backend errors
  }
}

/* ================= EVENT TYPE GUARD ================= */
function isEventDTO(data: unknown): data is EventDTO {
  return (
    !!data &&
    typeof data === "object" &&
    "eventName" in data &&
    typeof (data as any).eventName === "string"
  );
}

/* ================= ERROR EXTRACTOR ================= */
function getApiError(data: unknown): string {
  if (typeof data === "string" && data.trim()) return data;

  if (data && typeof data === "object") {
    const obj = data as any;

    if (typeof obj.message === "string") return obj.message;
    if (typeof obj.error === "string") return obj.error;
  }

  return "Event not found";
}

/* ================= COMPONENT ================= */
export default function EventManagement() {
  const { eventID } = useParams();
  const navigate = useNavigate();
  const apiFetch = useApiFetch();

  const [event, setEvent] = useState<EventDTO | null>(null);
  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");
  const [shouldRedirect, setShouldRedirect] = useState(false);

  /* ================= SAFE REDIRECT ================= */
  useEffect(() => {
    if (shouldRedirect && !error) {
      navigate("/", { replace: true });
    }
  }, [shouldRedirect, error, navigate]);

  function closePopup() {
    setError("");
    setShouldRedirect(true);
  }

  /* ================= LOAD EVENT ================= */
  useEffect(() => {
    let cancelled = false;

    async function loadEvent() {
      setLoading(true);
      setError("");
      setEvent(null);
      setShouldRedirect(false);

      try {
        if (!eventID) {
          setError("Missing event id");
          return;
        }

        /* ---------- 1. FETCH EVENT ---------- */
        const response = await apiFetch(`${API_BASE}/events/${eventID}`, {
          method: "GET",
        });

        const data = await readResponseBody(response);
        if (cancelled) return;

        if (!response.ok) {
          setError(getApiError(data));
          return;
        }

        if (!isEventDTO(data)) {
          setError("Invalid event response from server");
          return;
        }

        setEvent(data);

        /* ---------- 2. FETCH PERMISSIONS ---------- */
        const response2 = await apiFetch(
          `${API_BASE}/production-companies/${(data as any).eventProductionCompanyID}/me/permissions`,
          { method: "GET" },
        );

        const permissionsData = await readResponseBody(response2);
        if (cancelled) return;

        if (!response2.ok) {
          setError(getApiError(permissionsData));
          return;
        }

        if (!Array.isArray(permissionsData)) {
          setError("Invalid permissions response from server");
          return;
        }

        if (!permissionsData.includes("EVENT_INVENTORY")) {
          setError("User is not allowed to access this event");
          setShouldRedirect(true);
          return;
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : String(err));
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadEvent();

    return () => {
      cancelled = true;
    };
  }, [eventID, apiFetch]);

  /* ================= UI ================= */
  return (
    <div className="management-page">
      <div className="management-header">
        <h1>
          {loading ? "Loading event..." : event ? event.eventName : "Event"}
        </h1>
        <p>Event ID: {eventID ?? "Missing"}</p>
      </div>

      {/* POPUP */}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}>OK</button>
        </div>
      )}

      <div className="management-body">
        <aside className="management-sidebar">
          <NavLink to="show">Information</NavLink>
          <NavLink to="update-info">Update Information</NavLink>
          <NavLink to="lottery">Lottery</NavLink>
          <NavLink to="discount-policy">Discount Policies</NavLink>
          <NavLink to="purchase-policy">Purchase Policies</NavLink>
          <NavLink to="inventory">Inventory</NavLink>
          <NavLink to="pricing">Pricing</NavLink>
        </aside>

        <main className="management-content">
          {loading ? (
            <div>
              <h2>Loading...</h2>
              <p>Loading event data...</p>
            </div>
          ) : error ? (
            <div>
              <h2>Error</h2>
              <p>{error}</p>
            </div>
          ) : (
            <Outlet context={{ event }} />
          )}
        </main>
      </div>
    </div>
  );
}
