package backend.booking.controller;

import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResponse;
import backend.booking.dto.BookingResult;
import backend.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {

        BookingResult result = bookingService.createBooking(request);

        if ("WAITLISTED".equals(result.getType())) {
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(result.getWaitlistEntry());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result.getBooking());
    }

    @GetMapping("/student/{studentId}")
    public List<BookingResponse> getBookings(@PathVariable Long studentId) {
        return bookingService.getStudentBookings(studentId);
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
    }
}