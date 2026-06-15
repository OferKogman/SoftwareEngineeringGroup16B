import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { EventDTO } from "../../DTOs/EventDTO";
import { useApiFetch } from "../../apiFetch";

type CompanyEventListProps = {
  onEditEvent: (companyID: number, eventID: number) => void | Promise<void>;
  onDeleteEvent: (id: number) => void | Promise<void>;
};

export default function ViewCompanyEvents({
  onEditEvent,
  onDeleteEvent,
}: CompanyEventListProps) {
  const { companyID } = useParams();
  const [error, setError] = useState<string>("");
  const [eventDTOList, setEventDTOList] = useState<EventDTO[] | null>(null);

  const apiFetch = useApiFetch();

  useEffect(() => {
    if (!companyID) {
      return;
    }

    async function loadEvents() {
      try {
        const response = await apiFetch(
          `http://localhost:8080/production-companies/${companyID}/events`,
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error("Failed to load event.");
        }

        const eventList: EventDTO[] = await response.json();
        setEventDTOList(eventList);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load events.");
      }
    }

    void loadEvents();
  }, [companyID, apiFetch]);

  if (!companyID) {
    return <div>Missing company id</div>;
  }

  if (!eventDTOList) {
    return <div>No event's found for company</div>;
  }

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      {eventDTOList.length === 0 ? (
        <p>No events found</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {eventDTOList.map((event) => (
              <tr key={event.eventID}>
                <td>{event.eventID}</td>
                <td>{event.eventName}</td>
                <td>
                  <button
                    onClick={() =>
                      onEditEvent(event.eventProductionCompanyID, event.eventID)
                    }
                  >
                    Edit
                  </button>
                </td>
                <td>
                  <button onClick={() => onDeleteEvent(event.eventID)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
