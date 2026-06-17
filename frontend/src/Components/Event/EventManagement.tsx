import { useEffect, useState } from "react";
import { NavLink, useOutlet, useParams } from "react-router-dom";
import "../../CSS/Management.css";
import type { EventDTO } from "../../DTOs/EventDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import { useApiFetch } from "../../apiFetch";

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

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export default function EventManagement() {
  const { eventID } = useParams();
  const { sessionToken } = useSession();
  const outlet = useOutlet();

  const [event, setEvent] = useState<EventDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const apiFetch = useApiFetch();

  useEffect(() => {
    let cancelled = false;

    async function loadEvent() {
      setLoading(true);
      setError("");
      setEvent(null);

      if (!eventID) {
        setError("Missing event id");
        setLoading(false);
        return;
      }

      if (!sessionToken) {
        return;
      }

      try {
        const response = await apiFetch(`${API_BASE}/events/${eventID}`, {
          method: "GET",
        });

        const data = await readResponseBody(response);

        if (cancelled) {
          return;
        }

        if (!response.ok) {
          throw new Error(getApiError(data));
        }

        if (!isEventDTO(data)) {
          throw new Error("Invalid event response from server");
        }

        setEvent(data);
      } catch (error) {
        if (!cancelled) {
          setError(error instanceof Error ? error.message : "Event not found");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadEvent();

    return () => {
      cancelled = true;
    };
  }, [eventID, sessionToken, apiFetch]);

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

      {error && <p className="form-error">{error}</p>}

      <div className="management-body">
        <aside className="management-sidebar">
          <NavLink to="show">Information</NavLink>
          <NavLink to="update-info">Update Information</NavLink>
          <NavLink to="discount-policy">Update Discount Policies</NavLink>
          <NavLink to="purchase-policy">Update Purchase Policies</NavLink>
          <NavLink to="inventory">Inventory Management</NavLink>
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
            (outlet ?? (
              <div className="management-default-content">
                <h2>Event Management</h2>
                <p>Select an option from the sidebar.</p>
              </div>
            ))
          )}
        </main>
      </div>
    </div>
  );
}
