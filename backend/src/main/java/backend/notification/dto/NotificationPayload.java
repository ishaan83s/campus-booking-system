package backend.notification.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationPayload { Long recipientUserId; String type; Long slotId; String message; }
