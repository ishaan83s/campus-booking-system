package backend.professor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Returned by {@code ProfessorService.createProfile(userId, request)}
 * (Section 8.2). Internal shape only - called right after
 * AuthServiceImpl persists the User row (Section 6.1's interface
 * contract table). Not bound to any public controller endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorProfileResponse {
    private Long professorId;
    private Long userId;
    private String fullName;
    private String department;
    private String officeLocation;
    private String bio;
    private LocalDateTime createdAt;
}

