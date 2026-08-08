package backend.professor.dto;

import backend.common.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Section 10 - DTO Registry. Used by GET /api/professors/slots/{slotId}/bookings
 * (main spec Section 3.2). Assembled by SlotService.getBookingsForSlot(),
 * which reads booking/'s and waitlist/'s repositories read-only - the one
 * explicitly documented cross-module read exception in Section 16's
 * Integration Rules table.
 *
 * Design note: the two nested roster shapes below are not separately
 * named anywhere in the DTO Registry (Section 10) or in booking/'s or
 * waitlist/'s own DTO lists (Section 8.4 / 8.5). They are nested as
 * static inner classes here - rather than promoted to standalone,
 * separately-owned DTOs - so ownership of the whole response stays
 * unambiguous under professor/, while still matching the exact JSON
 * shape shown in the main spec's Section 3.2 example (bookingId,
 * studentId, studentName, rollNo, status, bookedAt / waitlistId,
 * studentId, studentName, position).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotBookingsResponse {

    private Long slotId;
    private List<BookingRosterEntry> bookings;
    private List<WaitlistRosterEntry> waitlist;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingRosterEntry {
        private Long bookingId;
        private Long studentId;
        private String studentName;
        private String rollNo;
        private BookingStatus status;
        private LocalDateTime bookedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaitlistRosterEntry {
        private Long waitlistId;
        private Long studentId;
        private String studentName;
        private Integer position;
    }
}