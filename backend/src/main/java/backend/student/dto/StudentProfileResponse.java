package backend.student.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {

    private Long studentId;
    private String fullName;
    private String rollNo;
    private Integer yearOfStudy;
}