package backend.booking.dto;

import backend.waitlist.dto.WaitlistEntryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Section 10 - DTO Registry: BookingResult.
 * Internal wrapper returned by BookingService.createBooking() (Section 8.4).
 * type: "BOOKED" | "WAITLISTED", plus either a BookingResponse or a
 * WaitlistEntryResponse - the controller unwraps this to decide 201 vs 202
 * (Section 3.3 decision logic, steps 4-5).
 *
 * Only one of booking / waitlistEntry is populated at a time, matching
 * whichever "type" is set.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResult {

    public static final String TYPE_BOOKED = "BOOKED";
    public static final String TYPE_WAITLISTED = "WAITLISTED";

    private String type;
    private BookingResponse booking;
    private WaitlistEntryResponse waitlistEntry;

    public static BookingResult booked(BookingResponse booking) {
        return BookingResult.builder()
                .type(TYPE_BOOKED)
                .booking(booking)
                .build();
    }

    public static BookingResult waitlisted(WaitlistEntryResponse waitlistEntry) {
        return BookingResult.builder()
                .type(TYPE_WAITLISTED)
                .waitlistEntry(waitlistEntry)
                .build();
    }
}