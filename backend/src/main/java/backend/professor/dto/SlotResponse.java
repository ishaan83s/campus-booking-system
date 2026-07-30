package backend.professor.dto;

import backend.common.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Section 10 - DTO Registry. Used by POST/PUT /api/professors/slots and
 * GET /api/professors/{professorId}/slots (main spec Section 3.2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {
    private Long slotId;
    private Long professorId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer bookedCount;
    private SlotStatus status;
}