package backend.professor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorProfileResponse {

    private Long professorId;

    private String fullName;

    private String department;

    private String officeLocation;

    private String bio;
}