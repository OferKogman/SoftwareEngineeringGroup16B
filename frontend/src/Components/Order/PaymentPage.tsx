import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import { useSession } from "../../GlobalContext/SessionContext";
import "./CSS/PaymentPage.css";
import PaymentForm from "./PaymentForm";

const API_BASE = "http://localhost:8080";
const PAYMENT_TIME_LIMIT_SECONDS = 10 * 60;

type PaymentLocationState = {
  orderId?: string;
  amount?: number;
};

type PaymentPayload = {
  firstName: string;
  lastName: string;
  idNumber: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  amount: number;
};

export default function PaymentPage() {
  const navigate = useNavigate();
  const apiFetch = useApiFetch();
  const location = useLocation();
  const { sessionToken } = useSession();

  const state = location.state as PaymentLocationState | null;
  const orderId = state?.orderId;

  const [amount, setAmount] = useState<number>(0);
  const [isLoadingPrice, setIsLoadingPrice] = useState<boolean>(true);

  const [orderStartTime, setOrderStartTime] = useState<number | null>(null);
  const [secondsLeft, setSecondsLeft] = useState(PAYMENT_TIME_LIMIT_SECONDS);
  const [error, setError] = useState("");
  const [isCanceling, setIsCanceling] = useState(false);

  function closePopup() {
    setError("");
  }

  useEffect(() => {
    async function getPrice() {
      const response = await apiFetch(
        `${API_BASE}/api/order/getOrderPrice/${orderId}`,
        {
          method: "GET",
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const data = await response.json();
      console.log("getOrderPrice response:", data);
      return data;
    }
    async function loadPrice() {
      if (!orderId || !sessionToken) {
        setIsLoadingPrice(false);
        return;
      }

      try {
        setError("");
        setIsLoadingPrice(true);

        const price = await getPrice();

        setAmount(price);

        const timestampResponse = await apiFetch(
          `${API_BASE}/api/order/getActiveOrderTimeStamp/${orderId}`,
          {
            method: "GET",
          },
        );

        if (!timestampResponse.ok) {
          throw new Error(await timestampResponse.text());
        }

        const timestamp = await timestampResponse.json();
        setOrderStartTime(timestamp);
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
      } finally {
        setIsLoadingPrice(false);
      }
    }

    void loadPrice();
  }, [orderId, sessionToken, apiFetch]);

  useEffect(() => {
    if (orderStartTime == null) {
      return;
    }

    const startTime = orderStartTime;

    function updateTimer() {
      const expiresAt = startTime + PAYMENT_TIME_LIMIT_SECONDS * 1000;
      const remainingMs = Math.max(0, expiresAt - Date.now());
      const remainingSeconds = Math.ceil(remainingMs / 1000);

      setSecondsLeft(remainingSeconds);
      if (remainingSeconds <= 0) {
        window.clearInterval(timerID);
        navigate("/");
      }
    }

    updateTimer();

    const timerID = window.setInterval(updateTimer, 1000);

    return () => window.clearInterval(timerID);
  }, [orderStartTime, navigate]);

  async function cancelOrder() {
    setIsCanceling(true);
    setError("");

    if (!orderId || !sessionToken) {
      navigate("/events/search");
      return;
    }

    try {
      const response = await apiFetch(
        `${API_BASE}/api/order/cancelOrder/${orderId}`,
        {
          method: "PUT",
          headers: {
            Accept: "application/json",
          },
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      navigate("/events/search");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsCanceling(false);
    }
  }

  function formatTimer(totalSeconds: number) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}:${seconds.toString().padStart(2, "0")}`;
  }

  async function completeOrder(paymentData: PaymentPayload) {
    if (!orderId) {
      throw new Error("Missing order ID.");
    }

    if (!sessionToken) {
      throw new Error("Missing session token.");
    }
    const [monthText, yearText] = paymentData.expiryDate.split("/");
    const month = Number(monthText);
    const year = 2000 + Number(yearText);
    const response = await apiFetch(
      `${API_BASE}/api/order/completeActiveOrder`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify({
          orderID: orderId,
          paymentInfo: {
            cardNumber: paymentData.cardNumber,
            month,
            year,
            holder: `${paymentData.firstName} ${paymentData.lastName}`,
            cvv: paymentData.cvv,
            id: paymentData.idNumber,
          },
        }),
      },
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data);
    }

    navigate("/events/search");
  }

  if (!orderId) {
    return (
      <div className="payment-page">
        <div className="settings-alert">
          <p>Missing order ID. Payment cannot continue.</p>
          <button onClick={closePopup}> OK </button>
        </div>
      </div>
    );
  }
  if (isLoadingPrice) {
    return (
      <div className="payment-page">
        <p>Loading payment amount...</p>
      </div>
    );
  }

  return (
    <div className="payment-page">
      <div className="payment-timer">Time left: {formatTimer(secondsLeft)}</div>

      <div className="payment-card">
        <h1>Payment</h1>

        {error && (
          <div className="settings-alert">
            <p>{error}</p>
            <button onClick={closePopup}> OK </button>
          </div>
        )}

        <PaymentForm
          initAmount={amount}
          orderID={orderId}
          onPaymentSubmit={async (paymentData) => {
            try {
              setError("");
              await completeOrder(paymentData);
            } catch (err) {
              setError(err instanceof Error ? err.message : "");
              throw err;
            }
          }}
        />

        <button
          className="payment-cancel-button"
          type="button"
          disabled={isCanceling}
          onClick={() => void cancelOrder()}
        >
          {isCanceling ? "Canceling..." : "Cancel Order"}
        </button>
      </div>
    </div>
  );
}
