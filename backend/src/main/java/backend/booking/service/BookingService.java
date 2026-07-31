package backend.booking.service;

import backend.booking.dto.BookingHistoryResponse;
import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResult;
import backend.common.enums.BookingStatus;
import backend.common.enums.Role;

public interface BookingService {

    /**
     * Creates a booking for the authenticated student.
     *
     * @param studentId authenticated student's ID
     * @param request booking request
     * @return BookingResult containing either BookingResponse
     *         or WaitlistEntryResponse (Phase 2)
     */
    BookingResult createBooking(Long studentId, BookingRequest request);

    /**
     * Cancels an existing booking.
     *
     * STUDENT -> can cancel only own booking
     * ADMIN -> can force cancel any booking
     *
     * @param requesterId authenticated user's ID
     * @param requesterRole authenticated user's role
     * @param bookingId booking ID
     */
    void cancelBooking(
            Long requesterId,
            Role requesterRole,
            Long bookingId
    );

    /**
     * Returns booking history for a student.
     *
     * @param studentId student ID
     * @param statusFilter optional status filter
     * @return booking history
     */
    BookingHistoryResponse getBookingHistory(
            Long studentId,
            BookingStatus statusFilter
    );
}