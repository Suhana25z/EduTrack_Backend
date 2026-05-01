package com.edutrack.backend.dto.attendance;

import java.time.LocalDate;

import com.edutrack.backend.entity.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private String studentId;
    private String studentName;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
}
