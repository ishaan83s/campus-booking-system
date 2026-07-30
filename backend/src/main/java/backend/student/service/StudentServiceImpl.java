package backend.student.service;

import backend.common.entity.User;
import backend.common.repository.UserRepository;
import backend.exception.ResourceNotFoundException;
import backend.student.dto.StudentProfileRequest;
import backend.student.dto.StudentProfileResponse;
import backend.student.exception.DuplicateRollNoException;
import backend.student.model.StudentProfile;
import backend.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StudentProfileResponse createProfile(
            Long userId,
            StudentProfileRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + userId + " not found"
                ));

        String rollNo = normalizeRollNo(request.rollNo());

        if (studentProfileRepository.existsByRollNo(rollNo)) {
            throw new DuplicateRollNoException(rollNo);
        }

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setUser(user);
        studentProfile.setRollNo(rollNo);
        studentProfile.setYearOfStudy(request.yearOfStudy());

        StudentProfile savedProfile =
                studentProfileRepository.save(studentProfile);

        return toResponse(savedProfile);
    }

    @Override
    public StudentProfileResponse getProfile(Long studentId) {
        StudentProfile studentProfile =
                findProfileByUserId(studentId);

        return toResponse(studentProfile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateProfile(
            Long studentId,
            StudentProfileRequest request
    ) {
        StudentProfile studentProfile =
                findProfileByUserId(studentId);

        String rollNo = normalizeRollNo(request.rollNo());

        boolean rollNoChanged =
                !studentProfile.getRollNo().equals(rollNo);

        if (rollNoChanged
                && studentProfileRepository.existsByRollNo(rollNo)) {
            throw new DuplicateRollNoException(rollNo);
        }

        studentProfile.setRollNo(rollNo);
        studentProfile.setYearOfStudy(request.yearOfStudy());

        StudentProfile updatedProfile =
                studentProfileRepository.save(studentProfile);

        return toResponse(updatedProfile);
    }

    private StudentProfile findProfileByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile for user id "
                                + userId
                                + " not found"
                ));
    }

    private String normalizeRollNo(String rollNo) {
        return rollNo.trim();
    }

    private StudentProfileResponse toResponse(
            StudentProfile studentProfile
    ) {
        return new StudentProfileResponse(
                studentProfile.getUser().getId(),
                studentProfile.getUser().getFullName(),
                studentProfile.getRollNo(),
                studentProfile.getYearOfStudy()
        );
    }
}