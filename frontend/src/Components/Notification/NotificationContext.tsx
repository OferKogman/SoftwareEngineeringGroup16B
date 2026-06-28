import React, { createContext, useCallback, useContext, useState } from "react";
import type { NotificationData } from "../../DTOs/NotificationTypesDTO";
import { generateNotificationId } from "./NotificationUtils";

interface NotificationContextType {
  notifications: NotificationData[]; // For the floating toasts
  inbox: NotificationData[]; // Persistent history for the Bell
  unreadCount: number;
  addNotification: (notification: Omit<NotificationData, "id">) => void;
  removeNotification: (id: string) => void;
  clearAllToasts: () => void;
  markInboxAsRead: () => void;
  clearInbox: () => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<NotificationData[]>([]);
  const [inbox, setInbox] = useState<NotificationData[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const removeNotification = useCallback((id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const addNotification = useCallback((notification: Omit<NotificationData, "id">) => {
    const id = generateNotificationId();
    const newNotification = { ...notification, id };

    setInbox((prev) => [newNotification, ...prev]);
    setUnreadCount((prev) => prev + 1);

    setNotifications((prev) => {
      const isDuplicate = prev.some((n) => n.message === notification.message && n.type === notification.type);
      if (isDuplicate) return prev;

      if (prev.length >= 3) return prev; 

      if (notification.duration) {
        setTimeout(() => removeNotification(id), notification.duration);
      }
      return [...prev, newNotification];
    });
  }, [removeNotification]);

  const clearAllToasts = useCallback(() => setNotifications([]), []);
  const markInboxAsRead = useCallback(() => setUnreadCount(0), []);
  const clearInbox = useCallback(() => {
    setInbox([]);
    setUnreadCount(0);
  }, []);

  return (
    <NotificationContext.Provider value={{ 
      notifications, inbox, unreadCount, addNotification, removeNotification, clearAllToasts, markInboxAsRead, clearInbox 
    }}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = (): NotificationContextType => {
  const context = useContext(NotificationContext);
  if (!context) throw new Error("useNotifications must be used within a NotificationProvider");
  return context;
};