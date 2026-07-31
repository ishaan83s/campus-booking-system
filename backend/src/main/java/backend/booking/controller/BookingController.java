package backend.booking.controller;

import backend.booking.dto.BookingHistoryResponse;
import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResult;
import backend.booking.service.BookingService;
import backend.common.entity.User;
import backend.common.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Student books a slot.
     *
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<?> createBooking(
            @AuthenticationPrincipal User user,
            @RequestBody BookingRequest request
    ) {

        BookingResult result =
                bookingService.createBooking(user.getId(), request);

        if ("WAITLISTED".equals(result.getType())) {
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(result.getWaitlist());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result.getBooking());
    }

    /**
     * Student/Admin cancels booking.
     *
     * DELETE /api/bookings/{bookingId}
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @AuthenticationPrincipal User user,
            @PathVariable Long bookingId
    ) {

        bookingService.cancelBooking(
                user.getId(),
                user.getRole(),
                bookingId
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Logged-in student's booking history.
     *
     * GET /api/bookings/me
     * GET /api/bookings/me?status=BOOKED
     */
    @GetMapping("/me")
    public ResponseEntity<BookingHistoryResponse> getMyBookings(

            @AuthenticationPrincipal User user,

            @RequestParam(required = false)
            BookingStatus status
    ) {

        BookingHistoryResponse response =
                bookingService.getBookingHistory(
                        user.getId(),
                        status
                );

        return ResponseEntity.ok(response);
    }

}