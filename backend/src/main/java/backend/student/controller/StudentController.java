package backend.student.controller;

import backend.student.service.StudentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Phase 2:
    // Student profile endpoints will be added after their exact
    // paths and authentication-to-studentId contract are confirmed.
}