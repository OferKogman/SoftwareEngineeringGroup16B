import React, { createContext, useState, useContext, useCallback, useEffect } from "react";
import type { NotificationData, NotificationType } from "../../DTOs/NotificationTypesDTO";
import { generateNotificationId } from "./NotificationUtils";
import { useSession } from "../../App";

interface NotificationContextType {
  notifications: NotificationData[];
  addNotification: (notification: Omit<NotificationData, "id">) => void;
  removeNotification: (id: string) => void;
  clearAll: () => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<NotificationData[]>([]);
  
  const { sessionToken } = useSession(); 

  const removeNotification = useCallback((id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

const addNotification = useCallback((notification: Omit<NotificationData, "id">) => {
    setNotifications((prev) => {
      // check if a notification with the exact same message and type is already on screen
      const isDuplicate = prev.some(
        (n) => n.message === notification.message && n.type === notification.type
      );

      if (isDuplicate) {
        return prev; 
      }

      const id = generateNotificationId();
      const newNotification = { ...notification, id };

      //set up the timeout if a duration was provided
      if (notification.duration) {
        setTimeout(() => {
          removeNotification(id);
        }, notification.duration);
      }

      //add notificationto the arr
      return [...prev, newNotification];
    });
  }, [removeNotification]);

  const clearAll = useCallback(() => {
    setNotifications([]);
  }, []);

  useEffect(() => {
    if (!sessionToken) return;


    const eventSource = new EventSource(
      `http://localhost:8080/api/notifications/stream?token=${sessionToken}`
    );


    eventSource.onmessage = (event) => {
      addNotification({
        type: "message", 
        message: event.data,
        duration: 5000, // auto-dismiss after 5 seconds
      });
    };

    eventSource.onerror = () => {
      console.error("Lost connection to notification broadcaster.");
      eventSource.close();
    };

    // When the component unmounts or the session ends, this cleanup function runs.
    // It closes the HTTP stream, which tells the Java backend to call `Broadcaster.removeListener()`.
    return () => {
      eventSource.close();
    };
  }, [sessionToken, addNotification]); 

  return (
    <NotificationContext.Provider value={{ notifications, addNotification, removeNotification, clearAll }}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = (): NotificationContextType => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error("useNotifications must be used within a NotificationProvider");
  }
  return context;
};