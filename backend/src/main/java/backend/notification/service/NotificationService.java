package backend.notification.service;
import backend.notification.dto.NotificationPayload;
public interface NotificationService { void sendBookingConfirmed(NotificationPayload payload); void sendBookingCancelled(NotificationPayload payload); void sendWaitlistPromoted(NotificationPayload payload); }
