package backend.professor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Section 10 - DTO Registry. Used by GET /api/professors (search/browse)
 * and internally by GET /api/professors/{professorId}/slots to resolve
 * the professor's display name. Field names/casing match main spec
 * Section 3.2 exactly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorResponse {
    private Long professorId;
    private String fullName;
    private String department;
    private String officeLocation;
    private String bio;
}