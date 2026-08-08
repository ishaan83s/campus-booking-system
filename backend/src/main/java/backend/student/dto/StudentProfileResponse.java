package backend.student.dto;

public record StudentProfileResponse(
        Long studentId,
        String fullName,
        String rollNo,
        Integer yearOfStudy
) {
}