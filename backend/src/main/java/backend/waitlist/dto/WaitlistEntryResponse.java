package backend.waitlist.dto;

import backend.common.enums.WaitlistStatus;
import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class WaitlistEntryResponse {
    Long waitlistId;
    Long slotId;
    Long studentId;
    WaitlistStatus status;
    Integer position;
    LocalDateTime createdAt;
}
