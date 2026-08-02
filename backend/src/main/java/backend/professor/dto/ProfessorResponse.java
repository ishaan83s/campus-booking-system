package backend.professor.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessorResponse {

    private Long id;

    private String fullName;

    private String department;

    private String officeLocation;

    private String bio;
}