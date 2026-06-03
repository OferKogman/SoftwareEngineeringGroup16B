import React from "react";
import { useNotifications } from "./NotificationContext";
import "./CSS/notifications.css";

export default function NotificationsContainer() {
  // Read the active notifications array and the remove function from context
  const { notifications, removeNotification } = useNotifications();

  // If there are no notifications, don't render anything
  if (notifications.length === 0) return null;

  return (
    <div className="notifications-container">
      {notifications.map((notif) => (
        <div key={notif.id} className={`notification-card notify-${notif.type}`}>
          <span>{notif.message}</span>
          <button 
            className="notification-close-btn" 
            onClick={() => removeNotification(notif.id)}
            aria-label="Close notification"
          >
            ✕
          </button>
        </div>
      ))}
    </div>
  );
}