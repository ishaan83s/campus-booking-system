package backend.booking.dto;

import backend.waitlist.dto.WaitlistEntryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryResponse {

    @Builder.Default
    private List<BookingResponse> bookings = new ArrayList<>();

    /**
     * Reserved for Phase 2 waitlist implementation.
     * Keep this field to maintain SSOT API compatibility.
     */
    @Builder.Default
    private List<WaitlistEntryResponse> waitlistEntries = new ArrayList<>();
}