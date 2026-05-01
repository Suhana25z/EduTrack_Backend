package com.edutrack.backend.dto.attendance;

import java.time.LocalDate;

import com.edutrack.backend.entity.AttendanceStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;
}
