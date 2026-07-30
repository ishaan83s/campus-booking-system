package backend.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Section 10 - DTO Registry: BookingRequest.
 * Used by: POST /api/bookings.
 * Fields (exact, camelCase per Section 10 casing rule): slotId.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "slotId is required")
    private Long slotId;
}