import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import ViewEvent from "./ViewEvent";

export default function EventInformation() {
  const navigate = useNavigate();
  const apiFetch = useApiFetch();
  const { eventID } = useParams();

  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function handleCreateOrder() {
    try {
      const response = await apiFetch(
        `http://localhost:8080/events/${eventID}/reservations/status`,
        {
          method: "GET",
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const status = await response.json();
      if (status === -1) {
        navigate("create-order");
      } else {
        navigate(`/events/${eventID}/queue`, {
          state: {
            initialStatus: status,
          },
        });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to enroll.");
    }
  }

  useEffect(() => {
    async function loadEvent() {
      if (!eventID) return;

      const response = await apiFetch(
        `http://localhost:8080/events/${eventID}`,
        {
          method: "GET",
        },
      );

      if (response.ok) {
        const data = await response.json();
        setEventDTO(data);
      }
    }

    loadEvent();
  }, [eventID, apiFetch]);

  async function enrollInLottery() {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/events/${eventID}/lottery/enroll`,
        {
          method: "POST",
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setMessage("Successfully enrolled in lottery.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to enroll.");
    }
  }

  function closePopup() {
    setMessage("");
    setError("");
  }

  const canEnroll =
    eventDTO?.lotteryDTO &&
    new Date(eventDTO.lotteryDTO.lotteryRegistrationDueDate) > new Date();

  return (
    <>
      <ViewEvent />

      {canEnroll ? (
        <button className="order-tickets-button" onClick={enrollInLottery}>
          Enroll In Lottery
        </button>
      ) : (
        <button
          className="order-tickets-button"
          onClick={() => handleCreateOrder()}
        >
          Order Tickets
        </button>
      )}

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button onClick={closePopup}>OK</button>
        </div>
      )}

      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}>OK</button>
        </div>
      )}
    </>
  );
}
