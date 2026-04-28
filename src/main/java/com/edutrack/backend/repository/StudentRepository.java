package com.edutrack.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.edutrack.backend.entity.Student;
import com.edutrack.backend.entity.User;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph(attributePaths = {"marks", "teacher", "user"})
    List<Student> findByTeacher(User teacher);

    @EntityGraph(attributePaths = {"marks", "teacher", "user"})
    Optional<Student> findByStudentId(String studentId);

    @EntityGraph(attributePaths = {"marks", "teacher", "user"})
    Optional<Student> findByUser(User user);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);
}
