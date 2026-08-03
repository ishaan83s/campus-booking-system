package backend.student.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {

    private Long id;

    private String fullName;

    private String rollNumber;

    private Integer yearOfStudy;

    private String department;
}