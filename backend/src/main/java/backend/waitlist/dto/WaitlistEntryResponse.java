package backend.waitlist.dto;

import backend.common.enums.WaitlistStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistEntryResponse {

    private Long waitlistId;

    private Long slotId;

    private Long studentId;

    private WaitlistStatus status;

    private Integer position;

    private LocalDateTime createdAt;
}