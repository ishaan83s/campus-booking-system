package backend.booking.dto;

import backend.waitlist.dto.WaitlistEntryResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResult {

    private String type;

    private BookingResponse booking;

    private WaitlistEntryResponse waitlistEntry;
}