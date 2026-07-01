import "./CSS/notifications.css";
import { useNotifications } from "./NotificationContext";

export default function NotificationsContainer() {
  const { notifications, removeNotification } = useNotifications();

  if (notifications.length === 0) return null;

  return (
    <div className="notifications-container">
      {notifications.map((notif) => (
        <div
          key={notif.id}
          className={`notification-card notify-${notif.type}`}
        >
          <div className="notification-content">
            <span className="notification-message">{notif.message}</span>
            
            {(notif.onAccept || notif.onReject) && (
              <div className="notification-actions">
                {notif.onAccept && (
                  <button
                    className="btn-accept"
                    onClick={() => {
                      notif.onAccept!();
                      removeNotification(notif.id); 
                    }}
                  >
                    Accept
                  </button>
                )}
                {notif.onReject && (
                  <button
                    className="btn-reject"
                    onClick={() => {
                      notif.onReject!();
                      removeNotification(notif.id); // Close after clicking
                    }}
                  >
                    Reject
                  </button>
                )}
              </div>
            )}
          </div>

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