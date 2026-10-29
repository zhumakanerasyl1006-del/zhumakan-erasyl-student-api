package kz.iitu.zhumakan.studentapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 40) String groupName,
        @NotBlank @Pattern(regexp = "[0-9]{1,30}", message = "must contain 1 to 30 digits")
        String studentId) {
}
