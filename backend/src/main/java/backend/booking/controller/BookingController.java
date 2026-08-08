package backend.booking.controller;

import backend.booking.dto.BookingHistoryResponse;
import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResponse;
import backend.booking.dto.BookingResult;
import backend.booking.service.BookingService;
import backend.common.enums.BookingStatus;
import backend.common.enums.Role;
import backend.waitlist.dto.WaitlistEntryResponse;
import backend.waitlist.service.WaitlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Section 19 - API Ownership Matrix (all four rows owned by Dev D):
 *   POST   /api/bookings                    Dev D
 *   GET    /api/bookings/me                 Dev D
 *   DELETE /api/bookings/{id}                Dev D
 *   DELETE /api/bookings/waitlist/{id}       Dev D
 *
 * Thin controller only: request validation (@Valid), DTO in / DTO out,
 * correct status codes (Section 12.3). No business logic - every branch
 * below is a straight pass-through to BookingService or WaitlistService
 * (Section 1.1 - Controller Layer rule).
 *
 * Role strings (STUDENT, PROFESSOR, ADMIN) and @PreAuthorize usage are
 * documented by Dev A / security/ (Section 6.1) - this controller assumes
 * that role wiring already exists.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final WaitlistService waitlistService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request,
                                           Authentication authentication) {
        Long studentId = currentUserId(authentication);
        BookingResult result = bookingService.createBooking(studentId, request);

        // Section 3.3, steps 4-5: 201 when a seat was available and
        // confirmed, 202 when the student was placed on the waitlist
        // instead (BookingResult.type decides which body/status to return).
        if (BookingResult.TYPE_BOOKED.equals(result.getType())) {
            BookingResponse body = result.getBooking();
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        }

        WaitlistEntryResponse body = result.getWaitlistEntry();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingHistoryResponse> getMyBookingHistory(
            @RequestParam(required = false) BookingStatus status,
            Authentication authentication) {
        Long studentId = currentUserId(authentication);
        BookingHistoryResponse history = bookingService.getBookingHistory(studentId, status);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId,
                                              Authentication authentication) {
        Long requesterId = currentUserId(authentication);
        Role requesterRole = currentUserRole(authentication);
        bookingService.cancelBooking(requesterId, requesterRole, bookingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/waitlist/{waitlistId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> leaveWaitlist(@PathVariable Long waitlistId,
                                              Authentication authentication) {
        Long studentId = currentUserId(authentication);
        // WaitlistService.leaveWaitlist() has no "internal-callers-only"
        // restriction (unlike joinWaitlist/promoteNext, Section 8.5) so the
        // controller is allowed to call it directly.
        waitlistService.leaveWaitlist(studentId, waitlistId);
        return ResponseEntity.noContent().build();
    }

    // TODO: awaiting security/ (Dev A) - this assumes an authentication
    // principal exposing the authenticated user's id and Role, per the
    // Section 12.4 rule that services take primitive IDs, never a raw
    // Authentication/Principal object, with translation happening once
    // here in the controller.
    private Long currentUserId(Authentication authentication) {
        return ((backend.security.UserPrincipal) authentication.getPrincipal()).getId();
    }

    private Role currentUserRole(Authentication authentication) {
        return ((backend.security.UserPrincipal) authentication.getPrincipal()).getRole();
    }
}