import { useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import EventSmallDisplay from "../Event/EventSmallDisplay";

export default function MainPage() {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const apiFetch = useApiFetch();

  useEffect(() => {
    async function loadEvents() {
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
        console.error(err);
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
      {events.map((event) => (
        <EventSmallDisplay key={event.eventID} event={event} />
      ))}
    </div>
  );
}
