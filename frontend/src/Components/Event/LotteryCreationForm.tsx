import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import "./CSS/LotteryCreationForm.css";

export default function LotteryCreationForm() {
  const { eventID } = useParams();

  const [lotteryName, setLotteryName] = useState("");
  const [winnerAmount, setWinnerAmount] = useState(1);
  const [lotteryRegistrationDueDate, setLotteryRegistrationDueDate] =
    useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const apiFetch = useApiFetch();
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    if (isSubmitting) return;

    setIsSubmitting(true);
    setMessage("");
    setError("");

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/events/${eventID}/lottery/policy`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            lotteryName,
            winnerAmount,
            lotteryRegistrationDueDate,
          }),
        },
      );

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Failed to create lottery.");
      }

      setMessage("Lottery created successfully.");

      setTimeout(() => {
        navigate(`/events/${eventID}/management/show`);
      }, 2000);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to create lottery.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  function closePopup() {
    setMessage("");
    setError("");
  }

  return (
    <main className="create-lottery-page">
      <header>
        <h2>Create Lottery For Upcoming Event</h2>
      </header>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="lotteryName">Lottery Name</label>
          <input
            id="lotteryName"
            type="text"
            value={lotteryName}
            onChange={(e) => setLotteryName(e.target.value)}
          />
        </div>

        <div>
          <label htmlFor="winnerAmount">Winner Amount</label>
          <input
            id="winnerAmount"
            type="number"
            value={winnerAmount}
            onChange={(e) => setWinnerAmount(Number(e.target.value))}
          />
        </div>

        <div>
          <label htmlFor="registrationDueDate">Registration Due Date</label>
          <input
            id="registrationDueDate"
            type="datetime-local"
            value={lotteryRegistrationDueDate}
            onChange={(e) => setLotteryRegistrationDueDate(e.target.value)}
          />
        </div>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create Lottery"}
        </button>
      </form>

      {message && (
        <div className="popup-alert">
          <p>{message}</p>
          <button onClick={closePopup}>OK</button>
        </div>
      )}

      {error && (
        <div className="popup-alert">
          <p>{error}</p>
          <button onClick={closePopup}>OK</button>
        </div>
      )}

      {message && (
        <div className="popup-alert">
          <p>{message}</p>
          <button
            onClick={closePopup}
            style={{
              padding: "0.8rem 1rem",
              borderRadius: "0.7rem",
              border: "none",
              cursor: "pointer",
              fontWeight: "bold",
            }}
          >
            OK
          </button>
        </div>
      )}

      {error && (
        <div className="popup-alert">
          <p>{error}</p>
          <button
            onClick={closePopup}
            style={{
              padding: "0.8rem 1rem",
              borderRadius: "0.7rem",
              border: "none",
              cursor: "pointer",
              fontWeight: "bold",
            }}
          >
            OK
          </button>
        </div>
      )}
    </main>
  );
}
