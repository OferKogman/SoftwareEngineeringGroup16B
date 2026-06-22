import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { EventDTO } from "../../DTOs/EventDTO";

type EventsListProps = {
  events?: EventDTO[] | null;
};

export default function ViewEvents({ events }: EventsListProps) {
  const [error, setError] = useState<string>("");

  const navigate = useNavigate();

  if (events && events.length === 0) {
    return <div>No events found</div>;
  }

  return (
    <div
      style={{ display: "flex", flexDirection: "column", alignItems: "center" }}
    >
      {error && <p className="form-error">{error}</p>}

      <table
        style={{
          alignItems: "center",
          width: "80%",
        }}
      >
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {events?.map((event) => (
            <tr key={event.eventID}>
              <td>{event.eventID}</td>
              <td>{event.eventName}</td>

              <td>
                <button onClick={() => navigate(`/events/${event.eventID}`)}>
                  View
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
