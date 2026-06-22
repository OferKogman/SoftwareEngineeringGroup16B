import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";

export default function VirtualQueue() {
  const location = useLocation();

  const initialStatus = (
    location.state as { initialStatus?: number } | undefined
  )?.initialStatus;

  const [position, setPosition] = useState<number | null>(
    initialStatus ?? null,
  );

  const [loading, setLoading] = useState(initialStatus === undefined);

  const { eventID } = useParams();
  const apiFetch = useApiFetch();
  const navigate = useNavigate();

  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    const fetchPosition = async () => {
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

          navigate(`/events/${eventID}/create-order`);
          return;
        }
      } catch (error) {
        console.error("Failed to refresh queue position:", error);
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
  }, [eventID, navigate, apiFetch]);

  if (loading && position === null) {
    return <div>Loading queue status...</div>;
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
      Waiting in queue. Your placement is {position}
    </div>
  );
}
