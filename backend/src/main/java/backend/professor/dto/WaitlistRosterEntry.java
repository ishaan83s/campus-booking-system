package backend.professor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistRosterEntry {

    private Long waitlistId;

    private Long studentId;

    private String studentName;

    private Integer position;

}