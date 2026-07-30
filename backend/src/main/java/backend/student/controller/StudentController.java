package backend.student.controller;

import backend.student.dto.StudentProfileRequest;
import backend.student.dto.StudentProfileResponse;
import backend.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> getMyProfile(
            @AuthenticationPrincipal(expression = "userId")
            Long studentId
    ) {
        StudentProfileResponse response =
                studentService.getProfile(studentId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<StudentProfileResponse> updateMyProfile(
            @AuthenticationPrincipal(expression = "userId")
            Long studentId,

            @Valid
            @RequestBody
            StudentProfileRequest request
    ) {
        StudentProfileResponse response =
                studentService.updateProfile(studentId, request);

        return ResponseEntity.ok(response);
    }
}