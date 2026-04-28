package com.edutrack.backend.controller;

import java.util.List;

import com.edutrack.backend.dto.attendance.AttendanceResponse;
import com.edutrack.backend.dto.dashboard.RecommendationResponse;
import com.edutrack.backend.dto.dashboard.StudentDashboardResponse;
import com.edutrack.backend.entity.User;
import com.edutrack.backend.service.AttendanceService;
import com.edutrack.backend.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentService.getStudentDashboard(student));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentService.getStudentRecommendations(student));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceResponse>> getAttendance(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(attendanceService.getStudentAttendance(student));
    }
}
