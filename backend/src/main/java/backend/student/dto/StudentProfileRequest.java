package backend.student.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentProfileRequest(

        @NotBlank(message = "rollNo is required")
        @Size(
                max = 50,
                message = "rollNo must not exceed 50 characters"
        )
        String rollNo,

        @Min(
                value = 1,
                message = "yearOfStudy must be at least 1"
        )
        @Max(
                value = 8,
                message = "yearOfStudy must not exceed 8"
        )
        Integer yearOfStudy
) {
}