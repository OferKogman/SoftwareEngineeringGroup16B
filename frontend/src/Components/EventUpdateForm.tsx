import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { EventDTO } from "../DTOs/EventDTO";

export type EventUpdateDetails = {
  venueID: string;
  name: string;
  startDate: string;
  endDate: string;
  artist: string;
  category: string;
};

type EventUpdateFormProps = {
  onUpdateEvent: (event: EventUpdateDetails) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: EventUpdateDetails = {
  venueID: "",
  name: "",
  startDate: "",
  endDate: "",
  artist: "",
  category: "",
};

export default function EventUpdateForm({
  onUpdateEvent,
  onCancel,
}: EventUpdateFormProps) {
  const { id } = useParams();
  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);

  const [formData, setFormData] = useState<EventUpdateDetails>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!id) {
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
          active: true,
          venueID: "Live Park",
          name: "Last Tour Ever",
          startTime: "2026-06-22T14:30",
          endTime: "2026-06-22T18:30",
          artist: "Queen",
          category: "Rock",
          productionCompanyID: 0,
          discountPolicy: [],
          purchasePolicy: [],
          price: 100000,
          rating: 5,
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
  }, [id]);

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
      await onUpdateEvent({
        ...formData,
        venueID: formData.venueID.trim(),
        name: formData.name.trim(),
        startDate: formData.startDate.trim(),
        endDate: formData.endDate.trim(),
        artist: formData.artist.trim(),
        category: formData.category.trim(),
      });
      setFormData(initialFormData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update event.");
    } finally {
      setIsSubmitting(false);
    }
  }

  if (!id) {
    return <p>Missing event ID.</p>;
  }

  if (!eventDTO) {
    return <p>Loading event...</p>;
  }

  return (
    <form className="event-update-form" onSubmit={handleSubmit}>
      <h2>Manage Event</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Venue ID
        <input
          type="text"
          pattern=".*\S.*"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Venue Name name cannot be empty or whitespace.",
            )
          }
          value={formData.venueID}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("venueID", event.target.value);
          }}
          placeholder={eventDTO.venueID}
        />
      </label>

      <label>
        Event name
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
          placeholder={eventDTO.name}
        />
      </label>

      <label>
        Start date
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
          placeholder={eventDTO.startTime}
        />
      </label>

      <label>
        End date
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
          placeholder={eventDTO.endTime}
        />
      </label>

      <label>
        Artist
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
          placeholder={eventDTO.artist}
        />
      </label>

      <label>
        Category
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
          placeholder={eventDTO.category}
        />
      </label>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
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
