export type NotificationType = "success" | "error" | "warning" | "info" | "message" | "timer";

export interface NotificationData {
  id: string;
  type: NotificationType;
  message: string;
  duration?: number; 
}
