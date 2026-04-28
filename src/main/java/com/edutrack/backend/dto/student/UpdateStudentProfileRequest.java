package com.edutrack.backend.dto.student;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentProfileRequest {

    @NotBlank(message = "Branch is required")
    private String branch;
}
