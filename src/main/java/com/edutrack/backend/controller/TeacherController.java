package com.edutrack.backend.controller;

import java.util.List;

import com.edutrack.backend.dto.attendance.AttendanceRequest;
import com.edutrack.backend.dto.attendance.AttendanceResponse;
import com.edutrack.backend.dto.common.ApiResponse;
import com.edutrack.backend.dto.dashboard.ClassSummaryResponse;
import com.edutrack.backend.dto.dashboard.ReportResponse;
import com.edutrack.backend.dto.student.CreateStudentRequest;
import com.edutrack.backend.dto.student.StudentResponse;
import com.edutrack.backend.dto.student.SubjectMarkRequest;
import com.edutrack.backend.dto.student.UpdateMarksRequest;
import com.edutrack.backend.dto.student.UpdateStudentProfileRequest;
import com.edutrack.backend.dto.subject.CreateSubjectRequest;
import com.edutrack.backend.dto.subject.SubjectResponse;
import com.edutrack.backend.entity.User;
import com.edutrack.backend.service.AttendanceService;
import com.edutrack.backend.service.StudentService;
import com.edutrack.backend.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final SubjectService subjectService;

    @PostMapping("/students")
    @Operation(summary = "Create a student under the logged-in teacher")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request,
                                                         @AuthenticationPrincipal User teacher) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request, teacher));
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getStudents(@AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(studentService.getTeacherStudents(teacher));
    }

    @PutMapping("/students/{studentId}/marks")
    public ResponseEntity<StudentResponse> updateMarks(@PathVariable String studentId,
                                                       @Valid @RequestBody UpdateMarksRequest request,
                                                       @AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(studentService.updateMarks(studentId, request, teacher));
    }

    @PutMapping("/students/{studentId}/profile")
    public ResponseEntity<StudentResponse> updateProfile(@PathVariable String studentId,
                                                         @Valid @RequestBody UpdateStudentProfileRequest request,
                                                         @AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(studentService.updateProfile(studentId, request, teacher));
    }

    @PostMapping("/students/{studentId}/subjects")
    public ResponseEntity<StudentResponse> addStudentSubject(@PathVariable String studentId,
                                                             @Valid @RequestBody SubjectMarkRequest request,
                                                             @AuthenticationPrincipal User teacher) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.addStudentSubject(studentId, request, teacher));
    }

    @DeleteMapping("/students/{studentId}/subjects/{subjectOrder}")
    public ResponseEntity<StudentResponse> removeStudentSubject(@PathVariable String studentId,
                                                                @PathVariable Integer subjectOrder,
                                                                @AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(studentService.removeStudentSubject(studentId, subjectOrder, teacher));
    }

    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse> deleteStudent(@PathVariable String studentId,
                                                     @AuthenticationPrincipal User teacher) {
        studentService.deleteStudent(studentId, teacher);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Student deleted successfully").build());
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponse>> getSubjects(@AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(subjectService.getSubjects(teacher));
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectResponse> addSubject(@Valid @RequestBody CreateSubjectRequest request,
                                                      @AuthenticationPrincipal User teacher) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.addSubject(request, teacher));
    }

    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse> removeSubject(@PathVariable Long subjectId,
                                                     @AuthenticationPrincipal User teacher) {
        subjectService.removeSubject(subjectId, teacher);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Subject removed successfully").build());
    }

    @GetMapping("/analytics")
    public ResponseEntity<ClassSummaryResponse> getAnalytics(@AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(studentService.getClassSummary(teacher));
    }

    @GetMapping("/reports")
    public ResponseEntity<ReportResponse> getReports(@AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(studentService.getTeacherReport(teacher));
    }

    @PostMapping("/attendance")
    public ResponseEntity<AttendanceResponse> markAttendance(@Valid @RequestBody AttendanceRequest request,
                                                             @AuthenticationPrincipal User teacher) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.markAttendance(request, teacher));
    }
}
