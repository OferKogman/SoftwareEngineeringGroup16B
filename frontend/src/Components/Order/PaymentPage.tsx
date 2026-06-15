import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useSession } from "../../GlobalContext/SessionContext";
import PaymentForm from "./PaymentForm";
import "./CSS/PaymentPage.css";

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

  async function getPrice(orderId: string, sessionToken: string) {
    try {
          const response = await fetch(`${API_BASE}/api/order/getOrderPrice/${orderId}`, {
              method: "PUT",
              headers: {
              Authorization: sessionToken,
              Accept: "application/json",
              },
          });
          const data = await response.json();
          console.log("getOrderPrice response:", data);
          return data.value;
      } catch (err) {
          console.error("Failed to fetch order price:", err);
          return 0;
      }
  }


export default function PaymentPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { sessionToken } = useSession();

  const state = location.state as PaymentLocationState | null;
  const orderId = state?.orderId;

  const [amount, setAmount] = useState<number>(0);
  const [isLoadingPrice, setIsLoadingPrice] = useState<boolean>(true);

  const [secondsLeft, setSecondsLeft] = useState(PAYMENT_TIME_LIMIT_SECONDS);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadPrice() {
      if (!orderId || !sessionToken) {
        setIsLoadingPrice(false);
        return;
      }

      try {
        setError("");
        setIsLoadingPrice(true);

        const price = await getPrice(orderId, sessionToken);

        setAmount(price);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load order price.");
      } finally {
        setIsLoadingPrice(false);
      }
    }

    void loadPrice();
  }, [orderId, sessionToken]);

  useEffect(() => {
    const timerID = window.setInterval(() => {
      setSecondsLeft((current) => Math.max(current - 1, 0));
    }, 1000);

    return () => window.clearInterval(timerID);
  }, []);

  useEffect(() => {
    if (secondsLeft > 0) {
        return;
    }

    void cancelOrder();
    }, [secondsLeft]);

  function formatTimer(totalSeconds: number) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}:${seconds.toString().padStart(2, "0")}`;
  }

  async function cancelOrder() {
    if (!orderId || !sessionToken) {
      navigate("/events/search");
      return;
    }

    try {
        await fetch(`${API_BASE}/api/order/cancelOrder/${orderId}`, {
            method: "PUT",
            headers: {
            Authorization: sessionToken,
            Accept: "application/json",
            },
        });
        } finally {
        navigate("/events/search");
        }
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
    const response = await fetch(
    `${API_BASE}/api/order/completeActiveOrder`,
    {
        method: "PUT",
        headers: {
        "Content-Type": "application/json",
        Authorization: sessionToken,
        Accept: "application/json",
        },
        body: JSON.stringify({
            orderID: orderId,
            paymentInfo: {
              cardNumber: paymentData.cardNumber,
              month: month,
              year: year,
              holder: `${paymentData.firstName} ${paymentData.lastName}`,
              cvv: paymentData.cvv,
              id: paymentData.idNumber,
            },
            }),
    }
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || "Failed to complete order.");
    }

    navigate("/events/search");
  }

  if (!orderId) {
    return (
      <div className="payment-page">
        <p className="payment-error">Missing order ID. Payment cannot continue.</p>
        <button onClick={() => navigate("/events/search")}>Back to Search</button>
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

        {error && <p className="payment-error">{error}</p>}

        <PaymentForm
          amount={amount}
          onPaymentSubmit={async (paymentData) => {
            try {
              setError("");
              await completeOrder(paymentData);
            } catch (err) {
              setError(err instanceof Error ? err.message : "Payment failed.");
              throw err;
            }
          }}
        />

        <button className="payment-cancel-button" type="button" onClick={cancelOrder}>
          Cancel Order
        </button>
      </div>
    </div>
  );
}