import { useParams } from "react-router-dom";

export default function ManageEvent() {
  const { companyId, eventId } = useParams();

  return (
    <div>
      <h2>Manage Event</h2>

      <p>Company ID: {companyId}</p>
      <p>Event ID: {eventId}</p>
    </div>
  );
}