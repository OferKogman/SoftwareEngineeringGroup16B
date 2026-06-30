import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import LotteryCreationForm from "./LotteryCreationForm";
import LotteryInformation from "./LotteryInformation";

export type LotteryDTO = {
  lotteryName: string;
  winnerAmount: number;
  lotteryRegistrationDueDate: string;
};
export default function EventLottery() {
  const { eventID } = useParams<{ eventID: string }>();
  const [lottery, setLottery] = useState<LotteryDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const apiFetch = useApiFetch();

  useEffect(() => {
    async function getLottery() {
      try {
        const response = await apiFetch(
          `http://localhost:8080/events/${eventID}`,
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          setLottery(null);
          return;
        }

        const data: EventDTO = await response.json();
        setLottery(data.lotteryDTO || null);
      } catch {
        setLottery(null);
      } finally {
        setLoading(false);
      }
    }
    void getLottery();
  }, [eventID, apiFetch]);

  if (!eventID) {
    return <p>No event ID provided</p>;
  }

  async function handleLotteryResults() {
    setIsSubmitting(true);
    setMessage("");
    setError("");

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/events/${eventID}/lottery/results`,
        {
          method: "POST",
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setMessage("Lottery results handled successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsSubmitting(false);
    }
  }

  function closePopup() {
    setMessage("");
    setError("");
  }

  if (loading) {
    return <p>Loading lottery...</p>;
  }

  return (
    <main className="event-lottery">
      {lottery ? (
        <>
          <LotteryInformation lottery={lottery} />

          <button
            style={{
              padding: "1rem 1.5rem",
              borderRadius: "0.7rem",
              border: "none",
              cursor: "pointer",
              fontWeight: "bold",
            }}
            disabled={isSubmitting}
            onClick={() => void handleLotteryResults()}
          >
            {isSubmitting
              ? "Handling..."
              : "Handle Lottery Results"}
          </button>
        </>
      ) : (
        <LotteryCreationForm />
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
    </main>
  );
}
