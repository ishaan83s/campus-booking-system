package backend.professor.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotRequest {

    private LocalDate slotDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;
}