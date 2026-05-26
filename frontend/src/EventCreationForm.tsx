import { useState } from "react";


export type EventCreationData = {   
  venueID: string;
  name: string;
  startDate: string;
  endDate: string;
  artist: string;
  category: string;
  price: number;
};

type EventCreationFormProps = {
  onCreateEvent: (event: EventCreationData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: EventCreationData = {
  venueID: "",
  name: "",
  startDate: "",
  endDate: "",
  artist: "",
  category: "",
  price: 0.0,
};

export default function EventCreationForm({
  onCreateEvent,
  onCancel,}: EventCreationFormProps) {
  const [formData, setFormData] =
    useState<EventCreationData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);


  function updateField<K extends keyof EventCreationData>(
      field: K,
      value: EventCreationData[K],
    ) {
      setFormData((current) => ({
        ...current,
        [field]: value,
      }));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault()

        setIsSubmitting(true);
        setError("");
    
        try {
          await onCreateEvent({
            ...formData,
            venueID: formData.venueID.trim(),
            name : formData.name.trim(),
            artist: formData.artist.trim(),
            category: formData.category.trim(),
          });
          setFormData(initialFormData);
        } catch (err) {
          setError(
            err instanceof Error
              ? err.message
              : "Failed to create event.",
          );
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
          onInvalid={(event) => event.currentTarget.setCustomValidity("venue ID name cannot be empty or whitespace.")}
          value={formData.venueID}
          onChange={(event) => 
            {
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
          onInvalid={(event) => event.currentTarget.setCustomValidity("Event name cannot be empty or whitespace.")}
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
            value={formData.startDate}
            onInvalid={(event) => event.currentTarget.setCustomValidity("Please enter a valid start date and time.")}
            onChange={(event) => {
              event.currentTarget.setCustomValidity("");
              updateField("startDate", event.target.value);
            }}
            placeholder="Start date"
        />
      </label>

      <label>
        End date
        <input
            type="datetime-local"
            required
            min ={getMinimumEndDateTime(formData.startDate)}
            value={formData.endDate}
            onInvalid={(event) => event.currentTarget.setCustomValidity("Please enter a valid end date and time.\nMust be after now and start time.")}
            onChange={(event) => {
              event.currentTarget.setCustomValidity("");
              updateField("endDate", event.target.value);
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
          onInvalid={(event) => event.currentTarget.setCustomValidity("Artist name cannot be empty or whitespace.")}
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
          onInvalid={(event) => event.currentTarget.setCustomValidity("Category cannot be empty or whitespace.")}
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

    return startDate > localDate.toISOString().slice(0, 16) ? startDate : localDate.toISOString().slice(0, 16);
}