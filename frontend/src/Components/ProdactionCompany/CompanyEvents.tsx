import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useSession } from "../../App";
import type { EventDTO } from "../../DTOs/EventDTO";
import "./CSS/CompanyEvents.css";

const API_BASE = "http://localhost:8080";

export default function CompanyEvents() {
  const navigate = useNavigate();
  const { companyId } = useParams();
  const { sessionToken } = useSession();

  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadEvents() {
      if (!companyId || !sessionToken) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const response = await fetch(
          `${API_BASE}/production-companies/${companyId}/events`,
          {
            method: "GET",
            headers: {
              Authorization: sessionToken,
              Accept: "application/json",
            },
          },
        );

        const data = await response.json();

        if (!response.ok) {
          throw new Error(
            data?.message || data?.error || "Failed to load events",
          );
        }

        if (!cancelled) {
          setEvents(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error ? err.message : "Failed to load events",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadEvents();

    return () => {
      cancelled = true;
    };
  }, [companyId, sessionToken]);

  function handleCreateEvent() {
    navigate(`/companies/${companyId}/events/create`);
  }

  function handleManageEvent(eventId: number) {
    navigate(`/events/${eventId}/management`);
  }

  return (
    <div className="company-events-page">
      <div className="company-events-header">
        <h2>Company Events</h2>

        <button className="create-event-button" onClick={handleCreateEvent}>
          Create New Event
        </button>
      </div>

      {loading && <p>Loading events...</p>}
      {error && <p className="form-error">{error}</p>}

      <div className="company-events-list">
        {events.map((event) => (
          <div key={event.eventID} className="company-event-card">
            <div>
              <h3>Event Name: {event.eventName}</h3>
              <p>Event ID: {event.eventID}</p>
            </div>

            <button
              className="manage-event-button"
              onClick={() => handleManageEvent(event.eventID)}
            >
              Manage Event
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
