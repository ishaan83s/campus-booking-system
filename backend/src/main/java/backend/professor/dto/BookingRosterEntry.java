package backend.professor.dto;

import backend.common.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRosterEntry {

    private Long bookingId;

    private Long studentId;

    private String studentName;

    private String rollNo;

    private BookingStatus status;

    private LocalDateTime bookedAt;

}