package com.edutrack.backend.dto.student;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMarksRequest {

    @Valid
    @NotEmpty(message = "Marks are required")
    private List<SubjectMarkRequest> marks;
}
