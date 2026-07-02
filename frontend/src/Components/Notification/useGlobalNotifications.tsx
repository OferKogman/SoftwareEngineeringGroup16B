import { fetchEventSource } from "@microsoft/fetch-event-source";
import { useEffect } from "react";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";
import { useSession } from "../../GlobalContext/SessionContext";
import { useNotifications } from "./NotificationContext";

export function useGlobalNotifications() {
  const { sessionToken } = useSession();
  const { loggedIn } = useLoggedIn();

  const { addNotification, clearInbox, clearAllToasts } = useNotifications();

  useEffect(() => {
    clearInbox();
    clearAllToasts();

    if (!sessionToken || !loggedIn) {
      console.log("Notification stream not opened: no signed user session yet");
      return;
    }

    const abortController = new AbortController();

    const connectStream = async () => {
      try {
        console.log("Opening notification stream...");

        await fetchEventSource("http://localhost:8080/notifications/stream", {
          method: "GET",
          headers: {
            Authorization: sessionToken,
            Accept: "text/event-stream",
          },
          signal: abortController.signal,
          openWhenHidden: true,

          async onopen(response) {
            console.log("Notification stream response:", response.status);

            if (response.ok) {
              console.log("Connected to global notification stream!");
              return;
            }

            throw new Error(`Notification stream rejected: ${response.status}`);
          },

          onmessage(event) {
            console.log("Notification SSE event:", event.event, event.data);

            if (event.event === "connected") {
              console.log("Notification stream connected event received");
              return;
            }

            if (event.event !== "notification") {
              return;
            }

            const messageText = event.data || "New notification";

            addNotification({
              type: "message",
              message: messageText,
              duration: 10000,
            });
          },

          onerror(error) {
            console.error("Notification stream error:", error);
            throw error;
          },
        });
      } catch (error) {
        if (!abortController.signal.aborted) {
          console.error("Notification stream setup failed:", error);
        }
      }
    };

    connectStream();

    return () => {
      abortController.abort();
    };
  }, [
    sessionToken,
    loggedIn,
    addNotification,
    clearInbox,
    clearAllToasts,
  ]);
}