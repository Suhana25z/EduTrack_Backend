package com.edutrack.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edutrack.backend.entity.AttendanceRecord;
import com.edutrack.backend.entity.Student;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByStudent(Student student);

    boolean existsByStudentAndAttendanceDate(Student student, LocalDate attendanceDate);
}
