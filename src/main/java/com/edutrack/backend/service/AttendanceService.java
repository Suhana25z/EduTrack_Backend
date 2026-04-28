package com.edutrack.backend.service;

import java.util.List;

import com.edutrack.backend.dto.attendance.AttendanceRequest;
import com.edutrack.backend.dto.attendance.AttendanceResponse;
import com.edutrack.backend.entity.AttendanceRecord;
import com.edutrack.backend.entity.Student;
import com.edutrack.backend.entity.User;
import com.edutrack.backend.exception.BadRequestException;
import com.edutrack.backend.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request, User teacher) {
        Student student = studentService.getTeacherOwnedStudent(request.getStudentId(), teacher);
        if (attendanceRepository.existsByStudentAndAttendanceDate(student, request.getAttendanceDate())) {
            throw new BadRequestException("Attendance already recorded for this date");
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .student(student)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .build();
        return toResponse(attendanceRepository.save(record));
    }

    public List<AttendanceResponse> getStudentAttendance(User user) {
        Student student = studentService.getStudentByUser(user);
        return attendanceRepository.findByStudent(student).stream()
                .map(this::toResponse)
                .toList();
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        return AttendanceResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent().getStudentId())
                .studentName(record.getStudent().getName())
                .attendanceDate(record.getAttendanceDate())
                .status(record.getStatus())
                .build();
    }
}
