package backend.student.service;

import backend.common.entity.User;
import backend.common.repository.UserRepository;
import backend.student.dto.StudentProfileRequest;
import backend.student.dto.StudentProfileResponse;
import backend.student.entity.StudentProfile;
import backend.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentProfileRepository studentRepository;
    private final UserRepository userRepository;

    public StudentServiceImpl(
            StudentProfileRepository studentRepository,
            UserRepository userRepository) {

        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public StudentProfileResponse getStudentProfile(Long id) {

        StudentProfile student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return StudentProfileResponse.builder()
                .id(student.getId())
                .fullName(student.getUser().getFullName())
                .rollNumber(student.getRollNumber())
                .yearOfStudy(student.getYearOfStudy())
                .department(student.getDepartment())
                .build();
    }

    @Override
    public StudentProfileResponse createStudentProfile(StudentProfileRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudentProfile student = StudentProfile.builder()
                .user(user)
                .rollNumber(request.getRollNumber())
                .yearOfStudy(request.getYearOfStudy())
                .department(request.getDepartment())
                .build();

        StudentProfile savedStudent = studentRepository.save(student);

        return StudentProfileResponse.builder()
                .id(savedStudent.getId())
                .fullName(savedStudent.getUser().getFullName())
                .rollNumber(savedStudent.getRollNumber())
                .yearOfStudy(savedStudent.getYearOfStudy())
                .department(savedStudent.getDepartment())
                .build();
    }
}