package backend.student.controller;

import backend.student.dto.StudentProfileRequest;
import backend.student.dto.StudentProfileResponse;
import backend.student.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentProfileResponse createStudentProfile(
            @RequestBody StudentProfileRequest request) {

        return studentService.createStudentProfile(request);
    }

    @GetMapping("/{id}")
    public StudentProfileResponse getStudentProfile(@PathVariable Long id) {
        return studentService.getStudentProfile(id);
    }
}