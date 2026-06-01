import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { EventDTO } from "../../DTOs/EventDTO";

type EventsListProps = {
  events?: EventDTO[] | null;
};

export default function ViewEvents({ events }: EventsListProps) {
  const [error, setError] = useState<string>("");
  const [eventDTOList, setEventDTOList] = useState<EventDTO[]>([]);

  const navigate = useNavigate();

  useEffect(() => {
    async function loadEvents() {
      try {
        if (events !== undefined && events !== null) {
          setEventDTOList(events);
          return;
        }

        // Fake backend data for testing
        const eventList: EventDTO[] = [
          {
            eventID: 0,
            active: true,
            venueID: "Live Park",
            name: "Last Tour Ever",
            startTime: "2027-06-22T14:30",
            endTime: "2027-06-22T18:30",
            artist: "Queen",
            category: "Rock",
            productionCompanyID: 0,
            discountPolicy: null,
            purchasePolicy: null,
            price: 100000,
            rating: 5,
          },
          {
            eventID: 1,
            active: true,
            venueID: "Live Park",
            name: "Last Tour Ever For Real This Time",
            startTime: "2028-06-22T14:30",
            endTime: "2028-06-22T18:30",
            artist: "Queen",
            category: "Rock",
            productionCompanyID: 0,
            discountPolicy: null,
            purchasePolicy: null,
            price: 10000000,
            rating: 5,
          },
        ];

        setEventDTOList(eventList);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load events.");
      }
    }

    void loadEvents();
  }, [events]);

  if (eventDTOList.length === 0) {
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
          {eventDTOList.map((event) => (
            <tr key={event.eventID}>
              <td>{event.eventID}</td>
              <td>{event.name}</td>

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
