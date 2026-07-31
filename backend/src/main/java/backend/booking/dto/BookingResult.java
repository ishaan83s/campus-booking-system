package backend.booking.dto;

import backend.waitlist.dto.WaitlistEntryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResult {

    /**
     * BOOKED or WAITLISTED
     */
    private String type;

    private BookingResponse booking;

    /**
     * Reserved for Phase 2.
     */
    private WaitlistEntryResponse waitlist;

    public static BookingResult booked(BookingResponse booking) {
        return BookingResult.builder()
                .type("BOOKED")
                .booking(booking)
                .build();
    }

    public static BookingResult waitlisted(WaitlistEntryResponse waitlist) {
        return BookingResult.builder()
                .type("WAITLISTED")
                .waitlist(waitlist)
                .build();
    }
}