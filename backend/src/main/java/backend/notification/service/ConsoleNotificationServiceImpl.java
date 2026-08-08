package backend.notification.service;
import backend.notification.dto.NotificationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service @Slf4j
public class ConsoleNotificationServiceImpl implements NotificationService {
    public void sendBookingConfirmed(NotificationPayload payload) { log.info("Booking confirmed: {}", payload); }
    public void sendBookingCancelled(NotificationPayload payload) { log.info("Booking cancelled: {}", payload); }
    public void sendWaitlistPromoted(NotificationPayload payload) { log.info("Waitlist promoted: {}", payload); }
}
