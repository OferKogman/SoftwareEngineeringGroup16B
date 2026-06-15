import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { EventDTO } from "../../DTOs/EventDTO";
import "./CSS/EventUpdateForm.css";

const API_BASE = "http://localhost:8080";

export type EventUpdateDetails = {
  venueID: string;
  name: string;
  startDate: string;
  endDate: string;
  artist: string;
  category: string;
};

const initialFormData: EventUpdateDetails = {
  venueID: "",
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
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!eventID) {
      return;
    }

    async function loadEvent() {
      try {
        //const response = await fetch(`/api/events/${id}`);

        //if (!response.ok) {
        //  throw new Error("Failed to load event.");
        //}

        //const event: EventDTO = await response.json();

        const event: EventDTO = {
          eventID: 0,
          eventStatus: true,
          eventVenueID: "Live Park",
          eventName: "Last Tour Ever",
          eventStartTime: "2026-06-22T14:30",
          eventEndTime: "2026-06-22T18:30",
          eventArtist: "Queen",
          eventCategory: "Rock",
          eventProductionCompanyID: 0,
          eventDiscountPolicy: null,
          eventPurchasePolicy: null,
          eventPrice: 100000,
          eventRating: 5,
        };
        setEventDTO(event);
        setFormData({
          venueID: "",
          name: "",
          startDate: "",
          endDate: "",
          artist: "",
          category: "",
        });
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event.");
      }
    }

    void loadEvent();
  }, [eventID]);

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
    setError("");

    try {
      const updatedEvent: EventUpdateDetails = {
        ...formData,
        venueID: formData.venueID.trim(),
        name: formData.name.trim(),
        startDate: formData.startDate.trim(),
        endDate: formData.endDate.trim(),
        artist: formData.artist.trim(),
        category: formData.category.trim(),
      };

      const response = await fetch(`/api/events/${eventID}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(updatedEvent),
      });

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Failed to update event.");
      }

      setFormData(initialFormData);
      alert("Event updated successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update event.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleToggleStatus() {
    if (!eventID || !eventDTO) return;

    try {
      setIsSubmitting(true);

      const endpoint = eventDTO.eventStatus
        ? `${API_BASE}/events/${eventID}/deactivate`
        : `${API_BASE}/events/${eventID}/activate`;

      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || "Failed to update status");
      }

      setEventDTO((prev) =>
        prev ? { ...prev, eventStatus: !prev.eventStatus } : prev,
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed");
    } finally {
      setIsSubmitting(false);
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

      {error && <p className="form-error">{error}</p>}

      <div className="status-toggle">
        <button
          type="button"
          onClick={handleToggleStatus}
          disabled={isSubmitting}
        >
          {eventDTO.eventStatus ? "Deactivate event" : "Activate event"}
        </button>
      </div>

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
          value={formData.venueID}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("venueID", event.target.value);
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
          value={formData.name}
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
          required={formData.endDate.trim() !== ""}
          value={formData.startDate}
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
          required={formData.startDate.trim() !== ""}
          min={getMinimumEndDateTime(formData.startDate)}
          value={formData.endDate}
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
          value={formData.artist}
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
          value={formData.category}
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
