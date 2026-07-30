package backend.booking.dto;

import backend.waitlist.dto.WaitlistEntryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Section 10 - DTO Registry: BookingHistoryResponse.
 * Used by: GET /api/bookings/me.
 * Fields (exact, camelCase): bookings: List<BookingResponse>,
 * waitlistEntries: List<WaitlistEntryResponse>.
 *
 * NOTE: WaitlistEntryResponse is owned by waitlist/ (Section 8.5) - the
 * waitlist/ package is generated next per the requested build order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingHistoryResponse {

    private List<BookingResponse> bookings;
    private List<WaitlistEntryResponse> waitlistEntries;
}