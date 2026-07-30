package backend.professor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Section 10 - DTO Registry: "internal only (called from AuthServiceImpl)".
 * Never bound directly to a public controller endpoint - AuthServiceImpl
 * builds this from the register() request fields when role == PROFESSOR
 * (main spec Section 3.1, "department is required only when role =
 * PROFESSOR").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorProfileRequest {

    @NotBlank(message = "department is required")
    @Size(max = 100)
    private String department;

    @Size(max = 150)
    private String officeLocation;

    @Size(max = 1000)
    private String bio;
}