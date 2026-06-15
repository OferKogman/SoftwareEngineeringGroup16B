import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useSession } from "../../GlobalContext/SessionContext";

export type EventCreationData = {
  venueID: string;
  name: string;
  startTime: string;
  endTime: string;
  artist: string;
  category: string;
  pcID: number;
  price: number;
  rating: number;
};

type EventCreationFormProps = {
  onCancel?: () => void;
};

export default function EventCreationForm({
  onCancel,
}: EventCreationFormProps) {
  const { companyId } = useParams();
  const navigate = useNavigate();

  const initialFormData: EventCreationData = {
    venueID: "",
    name: "",
    startTime: "",
    endTime: "",
    artist: "",
    category: "",
    pcID: Number(companyId),
    price: 0.0,
    rating: 0.0,
  };
  const [formData, setFormData] = useState<EventCreationData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { sessionToken } = useSession();

  function updateField<K extends keyof EventCreationData>(
    field: K,
    value: EventCreationData[K],
  ) {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  }

  function formatLocalDateTimeForApi(value: string): string {
    return value.length === 16 ? `${value}:00` : value;
  }

  function isValidLocalDateTimeInput(value: string): boolean {
    return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value);
  }

  async function createEvent(eventData: EventCreationData) {
    if (!companyId) {
      throw new Error("Missing company ID.");
    }
    if (!sessionToken) {
      throw new Error("Missing session token.");
    }

    console.log("companyId from route:", companyId);
    console.log("eventData:", eventData);

    const response = await fetch(`http://localhost:8080/events`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: sessionToken,
      },
      body: JSON.stringify(eventData),
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Failed to create event.");
    }
    navigate(`/companies/${companyId}/events`);
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setError("");

    if (!companyId) {
      setError("Missing company ID.");
      setIsSubmitting(false);
      return;
    }

    if (!isValidLocalDateTimeInput(formData.startTime)) {
      setError("Start date must use a valid 4-digit year.");
      setIsSubmitting(false);
      return;
    }

    if (!isValidLocalDateTimeInput(formData.endTime)) {
      setError("End date must use a valid 4-digit year.");
      setIsSubmitting(false);
      return;
    }

    const eventData: EventCreationData = {
      ...formData,
      venueID: formData.venueID.trim(),
      name: formData.name.trim(),
      artist: formData.artist.trim(),
      category: formData.category.trim(),
      startTime: formatLocalDateTimeForApi(formData.startTime),
      endTime: formatLocalDateTimeForApi(formData.endTime),
      pcID: Number(companyId),
      rating: 0.0,
    };
    if (!Number.isInteger(eventData.pcID) || eventData.pcID <= 0) {
      setError("Invalid company ID.");
      setIsSubmitting(false);
      return;
    }

    try {
      await createEvent(eventData);
      setFormData(initialFormData);
      alert(`Event "${eventData.name}" created successfully.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create event.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Create Event</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Venue ID
        <input
          type="text"
          required
          pattern=".*\S.*"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "venue ID name cannot be empty or whitespace.",
            )
          }
          value={formData.venueID}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("venueID", event.target.value);
          }}
          placeholder="Venue ID"
        />
      </label>

      <label>
        Event name
        <input
          type="text"
          required
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
          placeholder="Event name"
        />
      </label>

      <label>
        Start date
        <input
          type="datetime-local"
          required
          min="1000-01-01T00:00"
          max="9999-12-31T23:59"
          value={formData.startTime}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Please enter a valid start date and time.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("startTime", event.target.value);
          }}
          placeholder="Start date"
        />
      </label>

      <label>
        End date
        <input
          type="datetime-local"
          required
          max="9999-12-31T23:59"
          min={getMinimumEndDateTime(formData.startTime)}
          value={formData.endTime}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Please enter a valid end date and time.\nMust be after now and start time.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("endTime", event.target.value);
          }}
          placeholder="End date"
        />
      </label>

      <label>
        Artist
        <input
          type="text"
          required
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
          placeholder="Artist"
        />
      </label>

      <label>
        Category
        <input
          type="text"
          required
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
          placeholder="Category"
        />
      </label>

      <label>
        Price
        <input
          type="number"
          min="0"
          value={formData.price}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("price", Number(event.target.value));
          }}
        />
      </label>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create event"}
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
