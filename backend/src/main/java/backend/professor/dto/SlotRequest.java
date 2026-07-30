package backend.professor.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Section 10 - DTO Registry. Used by POST /api/professors/slots and
 * PUT /api/professors/slots/{slotId} (main spec Section 3.2).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotRequest {

    @NotNull(message = "slotDate is required")
    @Future(message = "slotDate must be in the future")
    private LocalDate slotDate;

    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    private LocalTime endTime;

    @NotNull(message = "capacity is required")
    @Min(value = 1, message = "capacity must be at least 1")
    private Integer capacity;
}