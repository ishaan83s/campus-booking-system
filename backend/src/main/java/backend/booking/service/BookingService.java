package backend.booking.service;

import backend.booking.dto.BookingHistoryResponse;
import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResult;
import backend.common.enums.BookingStatus;
import backend.common.enums.Role;

/**
 * Section 8.4 - Booking Module Exposes.
 * Signature agreed at kickoff (Section 6.1) - this is the interface
 * BookingController is thin against, and the only door AdminServiceImpl
 * is allowed to call through for a force-cancel (Section 8.4 rule /
 * Section 16 Integration Rules).
 */
public interface BookingService {

    /**
     * Section 3.3 decision logic. Returns a BookingResult wrapping EITHER a
     * confirmed BookingResponse (201) OR a WaitlistEntryResponse (202) -
     * see DTO Registry Section 10.6 (BookingResult).
     */
    BookingResult createBooking(Long studentId, BookingRequest request);

    /**
     * Section 3.3 cancellation + waitlist-promotion trigger.
     * requesterRole distinguishes a self-service STUDENT cancellation from
     * an ADMIN force-cancel (Section 1.2 / Section 8.7).
     */
    void cancelBooking(Long requesterId, Role requesterRole, Long bookingId);

    /**
     * Section 3.3 - GET /api/bookings/me. statusFilter may be null (no
     * filter applied), matching the optional ?status=BOOKED query param.
     */
    BookingHistoryResponse getBookingHistory(Long studentId, BookingStatus statusFilter);
}