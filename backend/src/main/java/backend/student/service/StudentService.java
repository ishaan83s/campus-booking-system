package backend.student.service;

import backend.student.dto.StudentProfileRequest;
import backend.student.dto.StudentProfileResponse;

public interface StudentService {

    StudentProfileResponse createProfile(
            Long userId,
            StudentProfileRequest request
    );

    StudentProfileResponse getProfile(Long studentId);

    StudentProfileResponse updateProfile(
            Long studentId,
            StudentProfileRequest request
    );
}