package backend.student.service;

import backend.common.entity.User;
import backend.common.repository.UserRepository;
import backend.exception.ResourceNotFoundException;
import backend.student.dto.StudentProfileRequest;
import backend.student.dto.StudentProfileResponse;
import backend.student.exception.DuplicateRollNoException;
import backend.student.mapper.StudentProfileMapper;
import backend.student.model.StudentProfile;
import backend.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final StudentProfileMapper studentProfileMapper;

    public StudentServiceImpl(
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository,
            StudentProfileMapper studentProfileMapper
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.studentProfileMapper = studentProfileMapper;
    }

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

        if (studentProfileRepository.existsByRollNo(request.getRollNo())) {
            throw new DuplicateRollNoException(
                    "A student profile with roll number "
                            + request.getRollNo()
                            + " already exists"
            );
        }

        StudentProfile studentProfile = new StudentProfile();

        studentProfile.setUser(user);
        studentProfile.setRollNo(request.getRollNo());
        studentProfile.setYearOfStudy(request.getYearOfStudy());

        StudentProfile savedProfile =
                studentProfileRepository.save(studentProfile);

        return studentProfileMapper.toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(Long studentId) {
        StudentProfile studentProfile =
                studentProfileRepository.findByUserId(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Student profile with user id "
                                        + studentId
                                        + " not found"
                        ));

        return studentProfileMapper.toResponse(studentProfile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateProfile(
            Long studentId,
            StudentProfileRequest request
    ) {
        StudentProfile studentProfile =
                studentProfileRepository.findByUserId(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Student profile with user id "
                                        + studentId
                                        + " not found"
                        ));

        boolean rollNumberChanged =
                !studentProfile.getRollNo().equals(request.getRollNo());

        if (rollNumberChanged
                && studentProfileRepository.existsByRollNo(
                request.getRollNo()
        )) {
            throw new DuplicateRollNoException(
                    "A student profile with roll number "
                            + request.getRollNo()
                            + " already exists"
            );
        }

        studentProfile.setRollNo(request.getRollNo());
        studentProfile.setYearOfStudy(request.getYearOfStudy());

        StudentProfile updatedProfile =
                studentProfileRepository.save(studentProfile);

        return studentProfileMapper.toResponse(updatedProfile);
    }
}