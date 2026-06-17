import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import "./CSS/CompanyEvents.css";

const API_BASE = "http://localhost:8080";

export default function CompanyEvents() {
  const navigate = useNavigate();
  const { companyId } = useParams();
  const { sessionToken } = useSession();

  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [updatingEventId, setUpdatingEventId] = useState<number | null>(null);

  const apiFetch = useApiFetch();

  useEffect(() => {
    let cancelled = false;

    async function loadEvents() {
      if (!companyId || !sessionToken) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const response = await apiFetch(
          `${API_BASE}/production-companies/${companyId}/events`,
          {
            method: "GET",
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
  }, [companyId, sessionToken, apiFetch]);

  function handleCreateEvent() {
    navigate(`/companies/${companyId}/events/create`);
  }

  function handleManageEvent(eventId: number) {
    navigate(`/events/${eventId}/management`);
  }

  async function handleEventActiveChange(
    eventId: number,
    shouldActivate: boolean,
  ) {
    if (!sessionToken) {
      setError("Missing session token.");
      return;
    }

    setError("");
    setUpdatingEventId(eventId);

    try {
      const endpoint = shouldActivate ? "activate" : "deactivate";

      const response = await fetch(
        `${API_BASE}/events/${eventId}/${endpoint}`,
        {
          method: "POST",
          headers: {
            Authorization: sessionToken,
            Accept: "application/json",
          },
        },
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Failed to ${endpoint} event.`);
      }

      setEvents((currentEvents) =>
        currentEvents.map((event) =>
          event.eventID === eventId
            ? {
                ...event,
                eventStatus: shouldActivate,
              }
            : event,
        ),
      );
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to update event status.",
      );
    } finally {
      setUpdatingEventId(null);
    }
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
              <h3>Event Name: {event.eventName ?? event.eventName}</h3>
              <p>Event ID: {event.eventID}</p>
              <p>Status: {event.eventStatus ? "Active" : "Inactive"}</p>
            </div>

            <label className="event-active-toggle">
              <span>{event.eventStatus ? "Active" : "Inactive"}</span>

              <input
                type="checkbox"
                checked={Boolean(event.eventStatus)}
                disabled={updatingEventId === event.eventID}
                onChange={(e) => {
                  void handleEventActiveChange(
                    event.eventID,
                    e.currentTarget.checked,
                  );
                }}
              />

              <strong className="event-active-slider" />
            </label>

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
