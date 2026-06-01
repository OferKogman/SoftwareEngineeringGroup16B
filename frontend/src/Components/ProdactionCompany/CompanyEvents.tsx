import { useNavigate, useParams } from "react-router-dom";
import "./CSS/CompanyEvents.css";

type CompanyEvent = {
  id: number;
  name: string;
};

export default function CompanyEvents() {
  const navigate = useNavigate();
  const { companyId } = useParams();

  // TEMP MOCK DATA
  const events: CompanyEvent[] = [
    { id: 101, name: "Rock Night" },
    { id: 102, name: "Jazz Festival" },
    { id: 103, name: "Standup Show" },
  ];

  function handleCreateEvent() {
  navigate(`/production-company-menegment/${companyId}/events/create`);
}

  function handleManageEvent(eventId: number) {
    navigate(
      `/production-company-menegment/${companyId}/events/${eventId}/manage`
    );
  }

  return (
    <div className="company-events-page">
      <div className="company-events-header">
        <h2>Company Events</h2>

        <button
          className="create-event-button"
          onClick={handleCreateEvent}
        >
          Create New Event
        </button>
      </div>

      <div className="company-events-list">
        {events.map((event) => (
          <div
            key={event.id}
            className="company-event-card"
          >
            <div>
              <h3>{event.name}</h3>
              <p>Event ID: {event.id}</p>
            </div>

            <button
              className="manage-event-button"
              onClick={() => handleManageEvent(event.id)}
            >
              Manage Event
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}