export type NotificationType = "success" | "error" | "warning" | "info" | "message" | "timer" | "action";

export interface NotificationData {
  id: string;
  type: NotificationType;
  message: string;
  duration?: number; 
  onAccept?: () => void;
  onReject?: () => void;
}
