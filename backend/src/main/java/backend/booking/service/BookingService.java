package backend.booking.service;

import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResponse;
import backend.booking.dto.BookingResult;

import java.util.List;

public interface BookingService {

    BookingResult createBooking(BookingRequest request);

    List<BookingResponse> getStudentBookings(Long studentId);

    void cancelBooking(Long bookingId);
}