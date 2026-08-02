package backend.booking.dto;

import backend.common.enums.BookingStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;

    private Long studentId;

    private Long slotId;

    private BookingStatus status;
}