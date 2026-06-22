import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { EventDTO } from "../DTOs/EventDTO";
import { useApiFetch } from "../apiFetch";
import PurchasePolicyTree from "./Shared/PurchasePolicyTree";

export default function EditPurchasePolicy() {
  const { eventID } = useParams();

  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);
  const [error, setError] = useState("");

  const apiFetch = useApiFetch();

  useEffect(() => {
    async function loadEvent() {
      if (!eventID) {
        setError("Missing event ID");
        return;
      }

      try {
        const response = await apiFetch(
          `http://localhost:8080/events/${eventID}`,
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const data: EventDTO = await response.json();

        setEventDTO(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event");
      }
    }

    void loadEvent();
  }, [eventID, apiFetch]);

  if (error) {
    return <p>{error}</p>;
  }

  if (!eventDTO) {
    return <p>Loading...</p>;
  }

  return (
    <main>
      <h2>Edit Purchase Policy</h2>

      <PurchasePolicyTree policy={eventDTO.eventPurchasePolicy} />

      {/* your edit controls here */}
    </main>
  );
}
