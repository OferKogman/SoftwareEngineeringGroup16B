import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import type { PurchasePolicyDTO } from "../../DTOs/PurchasePolicyDTO";
import ViewEvent from "./ViewEvent";

export default function EventInformation() {
  const navigate = useNavigate();
  const apiFetch = useApiFetch();
  const { eventID } = useParams();

  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [lotteryCode, setLotteryCode] = useState<string | null>(null);
  const [age, setAge] = useState<number | null>(null);

  async function handleCreateOrder() {
    try {
      if (age === -1) {
        throw new Error("Age is required");
      }
      if (lotteryCode === "") {
        throw new Error("Lottery code is required");
      }
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
        navigate("create-order", {
          state: {
            lotteryCode,
            age,
          },
        });
      } else {
        navigate(`/events/${eventID}/queue`, {
          state: {
            initialStatus: status,
            lotteryCode,
            age,
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

  function checkAgeRequired(purchasePolicy: PurchasePolicyDTO): boolean {
    return purchasePolicy.type === "AND" || purchasePolicy.type === "OR"
      ? checkAgeRequired(purchasePolicy.left) ||
          checkAgeRequired(purchasePolicy.right)
      : purchasePolicy.type === "MIN_AGE" || purchasePolicy.type === "MAX_AGE";
  }
  const ageRequired =
    eventDTO?.eventPurchasePolicy &&
    checkAgeRequired(eventDTO.eventPurchasePolicy);

  useEffect(() => {
    async function needCode() {
      if (eventDTO?.lotteryDTO && lotteryCode === null) {
        setLotteryCode("");
      }
    }
    void needCode();
  }, [eventDTO?.lotteryDTO, lotteryCode]);

  useEffect(() => {
    async function needAge() {
      if (ageRequired && age === null) {
        setAge(-1);
      }
    }
    void needAge();
  }, [ageRequired, age]);

  return (
    <>
      <ViewEvent />

      {canEnroll ? (
        <button className="order-tickets-button" onClick={enrollInLottery}>
          Enroll In Lottery
        </button>
      ) : (
        <div>
          {eventDTO?.lotteryDTO && (
            <div>
              <label>Lottery Code</label>
              <input
                id="lotteryCode"
                type="text"
                value={lotteryCode ?? ""}
                onChange={(e) => setLotteryCode(e.target.value)}
              />
            </div>
          )}
          {ageRequired && (
            <div>
              <label>Age</label>
              <input
                id="age"
                type="number"
                min="0"
                value={age ?? -1}
                onChange={(e) => setAge(Number(e.target.value))}
              />
            </div>
          )}
          <button
            className="order-tickets-button"
            onClick={() => handleCreateOrder()}
          >
            Order Tickets
          </button>
        </div>
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
