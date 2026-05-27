import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { EventDTO } from "../DTOs/EventDTO";

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

  useEffect(() => {
    if (!companyID) {
      return;
    }

    async function loadEvents() {
      try {
        //const response = await fetch(`/api/events/${id}`);

        //if (!response.ok) {
        //  throw new Error("Failed to load event.");
        //}

        //const event: EventDTO = await response.json();

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
  }, [companyID]);

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
                <td>{event.name}</td>
                <td>
                  <button
                    onClick={() =>
                      onEditEvent(event.productionCompanyID, event.eventID)
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
