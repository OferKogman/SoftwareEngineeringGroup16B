import { useEffect, useState } from "react";
import { useNotifications } from "./NotificationContext";

export function useTicketTimer(expirationTimestamp: number | null) {
  const { addNotification } = useNotifications();
  const [timeLeft, setTimeLeft] = useState<string | null>(null);

  useEffect(() => {
    if (!expirationTimestamp) return;

    const intervalId = setInterval(() => {
      const now = Date.now();
      const difference = expirationTimestamp - now;

      if (difference <= 0) {
        setTimeLeft("Expired");
        addNotification({ 
          type: "error", 
          message: "Your ticket reservation has expired.", 
          duration: 10000 
        });
        clearInterval(intervalId);
        return;
      }

      const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((difference % (1000 * 60)) / 1000);

      setTimeLeft(`${minutes}m ${seconds}s`);

      if (minutes === 5 && seconds === 0) {
        addNotification({ 
          type: "timer",
          message: "Warning: 5 minutes remaining to complete your purchase!", 
          duration: 8000 
        });
      }

      if (minutes === 1 && seconds === 0) {
        addNotification({ 
          type: "warning", 
          message: "Hurry! Only 1 minute remaining before your tickets are released!", 
          duration: 8000 
        });
      }

    }, 1000);

    return () => clearInterval(intervalId);
  }, [expirationTimestamp, addNotification]);

  return timeLeft;
}