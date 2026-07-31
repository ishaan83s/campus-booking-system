package backend.professor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotBookingsResponse {

    private Long slotId;

    private List<BookingRosterEntry> bookings;

    private List<WaitlistRosterEntry> waitlist;

}