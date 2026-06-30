import { useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import EventSmallDisplay from "../Event/EventSmallDisplay";

export default function MainPage() {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const apiFetch = useApiFetch();

  function closePopup() {
    setError("");
  }

  useEffect(() => {
    async function loadEvents() {
      setLoading(true);
      setError("");

      try {
        const response = await apiFetch("http://localhost:8080/events/search", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({}),
        });

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const eventList: EventDTO[] = await response.json();
        setEvents(eventList);
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
      } finally {
        setLoading(false);
      }
    }

    void loadEvents();
  }, [apiFetch]);

  return (
    <div
      style={{
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "center",
        gap: "1rem",
        padding: "2rem",
      }}
    >
      {loading && <p>Loading events...</p>}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      {events.map((event) => (
        <EventSmallDisplay key={event.eventID} event={event} />
      ))}
    </div>
  );
}
