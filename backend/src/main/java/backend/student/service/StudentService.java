package backend.student.service;

import backend.student.dto.StudentProfileRequest;
import backend.student.dto.StudentProfileResponse;

public interface StudentService {

    StudentProfileResponse getStudentProfile(Long id);

    StudentProfileResponse createStudentProfile(StudentProfileRequest request);
}