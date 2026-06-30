import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useSession } from "../../GlobalContext/SessionContext";
import { useApiFetch } from "../../apiFetch";
import "./CSS/EventCreationForm.css";

export type EventCreationData = {
  venueID: string;
  name: string;
  startTime: string;
  endTime: string;
  artist: string;
  category: string;
  pcID: number;
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
    rating: 0.0,
  };

  const [formData, setFormData] = useState<EventCreationData>(initialFormData);

  const [error, setError] = useState<string>("");
  const [message, setMessage] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { sessionToken } = useSession();
  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  function closeSuccessPopup() {
    setMessage("");
    navigate(`/companies/${companyId}/events`);
  }

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

    const response = await apiFetch(`http://localhost:8080/events`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(eventData),
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setIsSubmitting(true);
    setMessage("");
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

    try {
      await createEvent(eventData);
      setFormData(initialFormData);
      setMessage(`Event "${eventData.name}" created successfully.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="create-event-page">
      <form className="create-event-form" onSubmit={handleSubmit}>
        <h2>Create Event</h2>

        {message && (
          <div className="settings-alert">
            <p>{message}</p>
            <button onClick={closeSuccessPopup}> OK </button>
          </div>
        )}
        {error && (
          <div className="settings-alert">
            <p>{error}</p>
            <button onClick={closePopup}> OK </button>
          </div>
        )}

        <div className="create-event-field">
          <label>Venue ID</label>
          <input
            type="text"
            required
            value={formData.venueID}
            onChange={(e) => updateField("venueID", e.target.value)}
            placeholder="Venue ID"
          />
        </div>

        <div className="create-event-field">
          <label>Event Name</label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => updateField("name", e.target.value)}
            placeholder="Event name"
          />
        </div>

        <div className="create-event-field">
          <label>Start Date</label>
          <input
            type="datetime-local"
            required
            min={new Date().toISOString().slice(0, 16)}
            max="9999-12-31T23:59"
            value={formData.startTime}
            onChange={(e) => updateField("startTime", e.target.value)}
          />
        </div>

        <div className="create-event-field">
          <label>End Date</label>
          <input
            type="datetime-local"
            required
            min={new Date().toISOString().slice(0, 16)}
            max="9999-12-31T23:59"
            value={formData.endTime}
            onChange={(e) => updateField("endTime", e.target.value)}
          />
        </div>

        <div className="create-event-field">
          <label>Artist</label>
          <input
            type="text"
            required
            value={formData.artist}
            onChange={(e) => updateField("artist", e.target.value)}
            placeholder="Artist"
          />
        </div>

        <div className="create-event-field">
          <label>Category</label>
          <input
            type="text"
            required
            value={formData.category}
            onChange={(e) => updateField("category", e.target.value)}
            placeholder="Category"
          />
        </div>

        <div className="create-event-actions">
          {onCancel && (
            <button type="button" onClick={onCancel} disabled={isSubmitting}>
              Cancel
            </button>
          )}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Creating..." : "Create Event"}
          </button>
        </div>
      </form>
    </main>
  );
}
