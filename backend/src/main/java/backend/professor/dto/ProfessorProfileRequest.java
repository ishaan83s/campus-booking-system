package backend.professor.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessorProfileRequest {

    private Long userId;

    private String department;

    private String officeLocation;

    private String bio;
}