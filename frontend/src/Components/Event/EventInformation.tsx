import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import type { PurchasePolicyDTO } from "../../DTOs/PurchasePolicyDTO";
import "./CSS/ViewEvent.css";
import ViewEvent from "./ViewEvent";

export default function EventInformation() {
  const navigate = useNavigate();
  const apiFetch = useApiFetch();
  const { eventID } = useParams();

  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);
  const [companyPurchasePolicy, setCompanyPurchasePolicy] =
    useState<PurchasePolicyDTO | null>(null);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isOrdering, setIsOrdering] = useState(false);
  const [isEnrolling, setIsEnrolling] = useState(false);
  const [lotteryCode, setLotteryCode] = useState<string | null>(null);
  const [age, setAge] = useState<number | null>(null);

  async function handleCreateOrder() {
    setIsOrdering(true);
    setMessage("");
    setError("");

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
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsOrdering(false);
    }
  }

  useEffect(() => {
    async function loadEvent() {
      setMessage("");
      setError("");

      try {
        if (!eventID) return;

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

        const companyResponse = await apiFetch(
          `http://localhost:8080/production-companies/${data.eventProductionCompanyID}`,
          {
            method: "GET",
          },
        );

        if (!companyResponse.ok) {
          throw new Error(await companyResponse.text());
        }

        const companyData: ProductionCompanyDTO = await companyResponse.json();
        setCompanyPurchasePolicy(companyData.purchasePolicy);
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
      } finally {
        setIsLoading(false);
      }
    }

    void loadEvent();
  }, [eventID, apiFetch]);

  async function enrollInLottery() {
    setIsEnrolling(true);
    setMessage("");
    setError("");

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
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsEnrolling(false);
    }
  }

  function closePopup() {
    setMessage("");
    setError("");
  }

  const canEnroll =
    eventDTO?.lotteryDTO &&
    new Date(eventDTO.lotteryDTO.lotteryRegistrationDueDate) > new Date();

  function checkAgeRequired() {
    if (!(!eventDTO || eventDTO.eventPurchasePolicy === null)) {
      if (!(!companyPurchasePolicy || companyPurchasePolicy === null)) {
        return (
          checkAgeRequiredRec(eventDTO.eventPurchasePolicy) ||
          checkAgeRequiredRec(companyPurchasePolicy)
        );
      } else {
        return checkAgeRequiredRec(eventDTO.eventPurchasePolicy);
      }
    } else {
      if (!(!companyPurchasePolicy || companyPurchasePolicy === null)) {
        return checkAgeRequiredRec(companyPurchasePolicy);
      } else {
        return false;
      }
    }
  }

  function checkAgeRequiredRec(purchasePolicy: PurchasePolicyDTO): boolean {
    return purchasePolicy.type === "AND" || purchasePolicy.type === "OR"
      ? checkAgeRequiredRec(purchasePolicy.left) ||
          checkAgeRequiredRec(purchasePolicy.right)
      : purchasePolicy.type === "MIN_AGE" || purchasePolicy.type === "MAX_AGE";
  }
  const ageRequired = checkAgeRequired();

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

  if (isLoading) {
    return <p>Loading event information...</p>;
  }

  return (
    <>
      <ViewEvent />

      {canEnroll ? (
        <button
          className="order-tickets-button"
          disabled={isEnrolling}
          onClick={enrollInLottery}
        >
          {isEnrolling ? "Enrolling..." : "Enroll In Lottery"}
        </button>
      ) : (
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
          }}
        >
          {eventDTO?.lotteryDTO && (
            <div className="form-field">
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
            <div className="form-field">
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
            disabled={isOrdering}
            onClick={() => void handleCreateOrder()}
          >
            {isOrdering ? "Ordering..." : "Order Tickets"}
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
