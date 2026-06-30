import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";

export default function VirtualQueue() {
  const location = useLocation();

  const initialStatus = (
    location.state as { initialStatus?: number } | undefined
  )?.initialStatus;

  const lotteryCode = (location.state as { lotteryCode?: string } | null)
    ?.lotteryCode;

  const age = (location.state as { age?: number } | null)?.age;

  const [position, setPosition] = useState<number | null>(
    initialStatus ?? null,
  );

  const [loading, setLoading] = useState(initialStatus === undefined);
  const [error, setError] = useState("");

  const { eventID } = useParams();
  const apiFetch = useApiFetch();
  const navigate = useNavigate();

  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  function closePopup() {
    setError("");
  }

  useEffect(() => {
    const fetchPosition = async () => {
      setError("");
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
        setPosition(status);

        if (status === -1) {
          if (intervalRef.current) {
            clearInterval(intervalRef.current);
            intervalRef.current = null;
          }

          navigate(`/events/${eventID}/create-order`, {
            state: {
              lotteryCode,
              age,
            },
          });
          return;
        }
      } catch (error) {
        setError(error instanceof Error ? error.message : "");
      } finally {
        setLoading(false);
      }
    };

    fetchPosition();

    intervalRef.current = setInterval(fetchPosition, 30000);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [eventID, navigate, apiFetch, age, lotteryCode]);

  if (loading && position === null) {
    return <p>Loading queue status...</p>;
  }

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "60vh",
        fontSize: "1.5rem",
        textAlign: "center",
      }}
    >
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      Waiting in queue. Your placement is {position}
    </div>
  );
}
