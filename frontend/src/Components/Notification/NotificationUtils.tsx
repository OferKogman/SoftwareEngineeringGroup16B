export function generateNotificationId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2, 9);//some random function, could be changed but date is easily extracted herre
}