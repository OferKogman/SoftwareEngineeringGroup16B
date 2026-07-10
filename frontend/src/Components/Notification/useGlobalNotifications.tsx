import { useEffect } from "react";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";
import { useSession } from "../../GlobalContext/SessionContext";
import { useNotifications } from "./NotificationContext";

function notificationSocketUrl(token: string): string {
  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  return `${protocol}://localhost:8080/ws/notifications?token=${encodeURIComponent(token)}`;
}

export function useGlobalNotifications() {
  const { sessionToken } = useSession();
  const { loggedIn } = useLoggedIn();
  const { addNotification, clearInbox, clearAllToasts } = useNotifications();

  useEffect(() => {
    clearInbox();
    clearAllToasts();

    if (!sessionToken || !loggedIn) {
      return;
    }

    const socket = new WebSocket(notificationSocketUrl(sessionToken));

    socket.onopen = () => {
      console.log("Connected to notification WebSocket.");
    };

    socket.onmessage = (event) => {
      let messageText = event.data;

      try {
        const parsed = JSON.parse(event.data);
        messageText = parsed.message || event.data;
      } catch {
        // If it is not JSON, use the raw message.
      }

      addNotification({
        type: "message",
        message: messageText,
        duration: 10000,
      });
    };

    socket.onerror = (event) => {
      console.error("Notification WebSocket error:", event);
    };

    socket.onclose = (event) => {
      console.log("Notification WebSocket closed:", event.code, event.reason);
    };

    return () => {
      socket.close();
    };
  }, [sessionToken, loggedIn, addNotification, clearInbox, clearAllToasts]);
}