package backend.professor.dto;

import backend.common.enums.SlotStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponse {

    private Long id;

    // Professor information
    private Long professorId;

    private String professorName;

    private String department;

    private String officeLocation;

    // Slot information
    private LocalDate slotDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;

    private Integer bookedCount;

    private SlotStatus status;
}