import { fetchEventSource } from "@microsoft/fetch-event-source";
import { useEffect } from "react";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";
import { useSession } from "../../GlobalContext/SessionContext";
import { useApiFetch } from "../../apiFetch";
import { useNotifications } from "./NotificationContext";

export function useGlobalNotifications() {
  const { sessionToken } = useSession();
  const { loggedIn } = useLoggedIn();
  const apiFetch = useApiFetch();

  const { addNotification, clearInbox, clearAllToasts } = useNotifications();

  useEffect(() => {
    clearInbox();
    clearAllToasts();

    if (!sessionToken || !loggedIn) return;

    const abortController = new AbortController();

    const connectStream = async () => {
      try {
        await fetchEventSource(
          `http://localhost:8080/api/notifications/stream?token=${sessionToken}`,
          {
            fetch: fetch,
            signal: abortController.signal,
            async onopen(response) {
              if (response.ok) {
                console.log("Connected to global notification stream!");
                return;
              }
              if (response.status >= 400 && response.status < 600) {
                throw new Error(
                  `Server rejected connection: ${response.status}`,
                );
              }
            },
            onmessage(event) {
              let messageText = event.data;
              try {
                const parsed = JSON.parse(event.data);
                messageText = parsed.message || event.data;
              } catch (e) {
                console.log(e);
              }

              addNotification({
                type: "message",
                message: messageText,
                duration: 10000,
              });
            },
            onerror(err) {
              console.error("Lost broadcast connection:", err);
              throw err;
            },
          },
        );
      } catch (err) {
        console.error("Stream setup error:", err);
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
    apiFetch,
  ]);
}
