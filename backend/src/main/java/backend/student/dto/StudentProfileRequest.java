package backend.student.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileRequest {

    private Long userId;

    private String rollNumber;

    private Integer yearOfStudy;

    private String department;
}