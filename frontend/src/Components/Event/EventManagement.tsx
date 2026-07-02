import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import "../../CSS/Management.css";
import type { EventDTO } from "../../DTOs/EventDTO";

const API_BASE = "http://localhost:8080";

function getApiError(data: unknown): string {
  if (typeof data === "string" && data.trim()) return data;

  if (data && typeof data === "object") {
    if ("message" in data && typeof data.message === "string")
      return data.message;
    if ("error" in data && typeof data.error === "string") return data.error;
  }

  return "event not found";
}

function isEventDTO(data: unknown): data is EventDTO {
  return (
    !!data &&
    typeof data === "object" &&
    "eventName" in data &&
    typeof data.eventName === "string"
  );
}

async function readResponseBody(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export default function EventManagement() {
  const { eventID } = useParams();
  const navigate = useNavigate();
  const apiFetch = useApiFetch();

  const [event, setEvent] = useState<EventDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [shouldRedirect, setShouldRedirect] = useState(false);

  function closePopup() {
    setError("");
    if (shouldRedirect) {
      navigate("/", { replace: true });
    }
  }

  useEffect(() => {
    let cancelled = false;

    async function loadEvent() {
      setLoading(true);
      setError("");
      setEvent(null);
      setShouldRedirect(false);

      const fail = (msg: string, redirect = false) => {
        if (cancelled) return;

        setError(msg);
        setShouldRedirect(redirect);
        setLoading(false);
      };

      try {
        if (!eventID) {
          fail("Missing event id");
          return;
        }

        // 1. Load event
        const response = await apiFetch(`${API_BASE}/events/${eventID}`, {
          method: "GET",
        });

        const data = await readResponseBody(response);
        if (cancelled) return;

        if (!response.ok) {
          const msg = getApiError(data);

          const redirect =
            msg.toLowerCase().includes("not part") ||
            msg.toLowerCase().includes("not allowed") ||
            msg.toLowerCase().includes("unauthorized");

          fail(msg, redirect);
          return;
        }

        if (!isEventDTO(data)) {
          fail("Invalid event response from server");
          return;
        }

        setEvent(data);

        // 2. Permissions
        const response2 = await apiFetch(
          `${API_BASE}/production-companies/${data.eventProductionCompanyID}/me/permissions`,
          { method: "GET" },
        );

        const permissionsData = await readResponseBody(response2);
        if (cancelled) return;

        if (!response2.ok) {
          const msg = getApiError(permissionsData);

          const redirect =
            msg.toLowerCase().includes("not part") ||
            msg.toLowerCase().includes("not allowed") ||
            msg.toLowerCase().includes("unauthorized");

          fail(msg, redirect);
          return;
        }

        if (!Array.isArray(permissionsData)) {
          fail("Invalid permissions response from server");
          return;
        }

        if (!permissionsData.includes("EVENT_INVENTORY")) {
          fail("User is not allowed to access this event", true);
          return;
        }
      } catch (err) {
        if (!cancelled) {
          fail(err instanceof Error ? err.message : String(err));
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

  return (
    <div className="management-page">
      <div className="management-header">
        <h1>
          {loading
            ? "Loading event..."
            : event
              ? event.eventName
              : "Event unavailable"}
        </h1>
        <p>Event ID: {eventID ?? "Missing"}</p>
      </div>

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
          <NavLink to="discount-policy">Update Discount Policies</NavLink>
          <NavLink to="purchase-policy">Update Purchase Policies</NavLink>
          <NavLink to="inventory">Inventory Management</NavLink>
          <NavLink to="pricing">Pricing Management</NavLink>
        </aside>

        <main className="management-content">
          {loading ? (
            <div className="management-default-content">
              <h2>Loading...</h2>
              <p>Loading event data from the server.</p>
            </div>
          ) : error ? (
            <div className="management-default-content">
              <h2>Cannot load event management</h2>
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
