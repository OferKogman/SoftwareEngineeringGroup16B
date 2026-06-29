import { useEffect } from "react";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { useNotifications } from "./NotificationContext";
import { useSession } from "../../GlobalContext/SessionContext";
import { useApiFetch } from "../../apiFetch";

export function useGlobalNotifications() {
  const { sessionToken } = useSession();
  const apiFetch = useApiFetch();
  const { addNotification } = useNotifications();

  useEffect(() => {
    if (!sessionToken) return;

    const abortController = new AbortController();

    const connectStream = async () => {
      try {
        await fetchEventSource(
          `http://localhost:8080/api/notifications/stream?token=${sessionToken}`,
          {
            fetch: apiFetch as any,
            signal: abortController.signal,
            async onopen(response) {
              if (response.ok) {
                console.log("Connected to global notification stream!");
                return;
              }
              if (response.status >= 400 && response.status < 600) {
                throw new Error(`Server rejected connection: ${response.status}`);
              }
            },
            onmessage(event) {
              let messageText = event.data;
              try {
                const parsed = JSON.parse(event.data);
                messageText = parsed.message || event.data;
              } catch (e) {
              }

              addNotification({
                type: "message", 
                message: messageText,
                duration: 10000, 
              });
            },
            onerror(err) {
              console.error("Lost broadcast connection:", err);
            },
          }
        );
      } catch (err) {
        console.error("Stream setup error:", err);
      }
    };

    connectStream();

    return () => {
      abortController.abort();
    };
  }, [sessionToken, addNotification, apiFetch]);
}