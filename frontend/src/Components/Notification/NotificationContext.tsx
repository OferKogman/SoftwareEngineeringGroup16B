import React, { createContext, useCallback, useContext, useState } from "react";
import type { NotificationData } from "../../DTOs/NotificationTypesDTO";
import { generateNotificationId } from "./NotificationUtils";

interface NotificationContextType {
  notifications: NotificationData[];
  addNotification: (notification: Omit<NotificationData, "id">) => void;
  removeNotification: (id: string) => void;
  clearAll: () => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<NotificationData[]>([]);

  const removeNotification = useCallback((id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const addNotification = useCallback((notification: Omit<NotificationData, "id">) => {
    setNotifications((prev) => {
      const isDuplicate = prev.some(
        (n) => n.message === notification.message && n.type === notification.type
      );
      if (isDuplicate) return prev;

      const id = generateNotificationId();
      const newNotification = { ...notification, id };

      if (notification.duration) {
        setTimeout(() => removeNotification(id), notification.duration);
      }
      return [...prev, newNotification];
    });
  }, [removeNotification]);

  const clearAll = useCallback(() => setNotifications([]), []);

  return (
    <NotificationContext.Provider value={{ notifications, addNotification, removeNotification, clearAll }}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = (): NotificationContextType => {
  const context = useContext(NotificationContext);
  if (!context) throw new Error("useNotifications must be used within a NotificationProvider");
  return context;
};