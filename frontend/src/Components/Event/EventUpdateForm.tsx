import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import "./CSS/EventUpdateForm.css";

const API_BASE = "http://localhost:8080";

export type EventUpdateDetails = {
  venue: string | null;
  name: string | null;
  startDate: string | null;
  endDate: string | null;
  artist: string | null;
  category: string | null;
};

const initialFormData: EventUpdateDetails = {
  venue: "",
  name: "",
  startDate: "",
  endDate: "",
  artist: "",
  category: "",
};

export default function EventUpdateForm() {
  const { eventID } = useParams();
  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);

  const [formData, setFormData] = useState<EventUpdateDetails>(initialFormData);
  const [error, setError] = useState<string>("");
  const [message, setMessage] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [isToggling, setIsToggling] = useState(false);

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  useEffect(() => {
    if (!eventID) {
      return;
    }

    async function loadEvent() {
      try {
        const response = await apiFetch(`${API_BASE}/events/${eventID}`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const event: EventDTO = await response.json();
        setEventDTO(event);
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
      }
    }

    void loadEvent();
  }, [eventID, apiFetch]);

  function updateField<K extends keyof EventUpdateDetails>(
    field: K,
    value: EventUpdateDetails[K],
  ) {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setMessage("");
    setError("");

    try {
      const updatedEvent = Object.fromEntries(
        Object.entries(formData)
          .filter(([, value]) => value?.trim() !== "")
          .map(([key, value]) => [key, value!.trim()]),
      ) as Partial<EventUpdateDetails>;

      const response = await apiFetch(`${API_BASE}/events/${eventID}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(updatedEvent),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setFormData(initialFormData);
      setMessage("Event updated successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleEventActiveChange(shouldActivate: boolean) {
    if (!eventID || !eventDTO || isToggling) return;

    setIsToggling(true);
    setMessage("");
    setError("");

    try {
      const endpoint = shouldActivate ? "activate" : "deactivate";

      const response = await apiFetch(
        `${API_BASE}/events/${eventID}/${endpoint}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setEventDTO((prev) =>
        prev ? { ...prev, eventStatus: shouldActivate } : prev,
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsToggling(false);
    }
  }

  if (!eventID) {
    return <p>Missing event ID.</p>;
  }

  if (!eventDTO) {
    return <p>Loading event...</p>;
  }

  return (
    <form className="event-update-form" onSubmit={handleSubmit}>
      <h2>Manage Event</h2>

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}

      <label className="event-active-toggle">
        <span>{eventDTO.eventStatus ? "Active" : "Inactive"}</span>

        <input
          type="checkbox"
          checked={Boolean(eventDTO.eventStatus)}
          disabled={isToggling}
          onChange={(e) => {
            void handleEventActiveChange(e.currentTarget.checked);
          }}
        />

        <strong className="event-active-slider" />
      </label>

      <label className="form-label">
        <span>Venue ID</span>
        <input
          type="text"
          pattern=".*\S.*"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Venue name cannot be empty or whitespace.",
            )
          }
          value={formData.venue || ""}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("venue", event.target.value);
          }}
          placeholder={eventDTO.eventVenueID}
        />
      </label>

      <label className="form-label">
        <span>Event name</span>
        <input
          type="text"
          pattern=".*\S.*"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Event name cannot be empty or whitespace.",
            )
          }
          value={formData.name || ""}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("name", event.target.value);
          }}
          placeholder={eventDTO.eventName}
        />
      </label>

      <label className="form-label">
        <span>Start date</span>
        <input
          type="datetime-local"
          required={formData.endDate?.trim() !== ""}
          min={new Date().toISOString().slice(0, 16)}
          max="9999-12-31T23:59"
          value={formData.startDate || ""}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Please enter a valid start date and time.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("startDate", event.target.value);
          }}
          placeholder={eventDTO.eventStartTime}
        />
      </label>

      <label className="form-label">
        <span>End date</span>
        <input
          type="datetime-local"
          required={formData.startDate?.trim() !== ""}
          min={getMinimumEndDateTime(formData.startDate || "")}
          max="9999-12-31T23:59"
          value={formData.endDate || ""}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Please enter a valid end date and time.\nMust be after now and start time.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("endDate", event.target.value);
          }}
          placeholder={eventDTO.eventEndTime}
        />
      </label>

      <label className="form-label">
        <span>Artist</span>
        <input
          type="text"
          pattern=".*\S.*"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Artist name cannot be empty or whitespace.",
            )
          }
          value={formData.artist || ""}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("artist", event.target.value);
          }}
          placeholder={eventDTO.eventArtist}
        />
      </label>

      <label className="form-label">
        <span>Category</span>
        <input
          type="text"
          pattern=".*\S.*"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Category cannot be empty or whitespace.",
            )
          }
          value={formData.category || ""}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("category", event.target.value);
          }}
          placeholder={eventDTO.eventCategory}
        />
      </label>

      <div className="form-actions">
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Updating..." : "Update event"}
        </button>
      </div>
    </form>
  );
}

function getMinimumEndDateTime(startDate: string): string {
  const now = new Date();

  const offset = now.getTimezoneOffset();
  const localDate = new Date(now.getTime() - offset * 60 * 1000);

  return startDate > localDate.toISOString().slice(0, 16)
    ? startDate
    : localDate.toISOString().slice(0, 16);
}
