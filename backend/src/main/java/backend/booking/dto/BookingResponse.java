package backend.booking.dto;

import backend.common.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Section 10 - DTO Registry: BookingResponse.
 * Used by: 201 branch of POST /api/bookings, GET /api/bookings/me.
 * Fields (exact, camelCase): bookingId, slotId, studentId, status, bookedAt,
 * slotDate, startTime, endTime, professorName.
 *
 * Matches the exact shape shown in Section 3.3 example responses of the
 * main spec.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long slotId;
    private Long studentId;
    private BookingStatus status;
    private LocalDateTime bookedAt;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String professorName;
}